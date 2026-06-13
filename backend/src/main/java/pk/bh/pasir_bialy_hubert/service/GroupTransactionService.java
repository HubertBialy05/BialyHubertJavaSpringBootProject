package pk.bh.pasir_bialy_hubert.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pk.bh.pasir_bialy_hubert.dto.GroupTransactionDTO;
import pk.bh.pasir_bialy_hubert.model.*;
import pk.bh.pasir_bialy_hubert.repository.DebtRepository;
import pk.bh.pasir_bialy_hubert.repository.GroupRepository;
import pk.bh.pasir_bialy_hubert.repository.MembershipRepository;
import pk.bh.pasir_bialy_hubert.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GroupTransactionService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final MembershipService membershipService;
    private final TransactionRepository transactionRepository;   // DODANE
    private final SimpMessagingTemplate messagingTemplate;

    public GroupTransactionService(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            DebtRepository debtRepository,
            MembershipService membershipService,
            TransactionRepository transactionRepository,   // jeśli dodałeś wcześniej
            SimpMessagingTemplate messagingTemplate) {     // NOWY PARAMETR
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.membershipService = membershipService;
        this.transactionRepository = transactionRepository; // opcjonalnie
        this.messagingTemplate = messagingTemplate;        // DODANE
    }

    public void addGroupTransaction(GroupTransactionDTO transactionDTO, User currentUser) {
        Group group = groupRepository.findById(transactionDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Grupy"));

        membershipService.assertCurrentUserIsGroupMember(group.getId());

        List<Membership> members = membershipRepository.findByGroupId(group.getId());
        List<Membership> selectedMembers = selectParticipants(transactionDTO, members, currentUser);
        if (selectedMembers.isEmpty()) {
            throw new IllegalStateException("Grupa nie ma czlonkow, nie mozna dodac transakcji.");
        }
        double amountPerUser = transactionDTO.getAmount() / selectedMembers.size();
        boolean expense = "EXPENSE".equals(transactionDTO.getType());
        // Jeśli to wydatek, dodajemy transakcję dla aktualnego użytkownika (pełna kwota)
        if (expense) {
            Transaction userTransaction = new Transaction();
            userTransaction.setAmount(transactionDTO.getAmount());
            userTransaction.setType(TransactionType.EXPENSE);
            userTransaction.setTags("group expense");
            userTransaction.setNotes("Wydatek grupowy: " + transactionDTO.getTitle());
            userTransaction.setUser(currentUser);
            userTransaction.setTimestamp(LocalDateTime.now());
            transactionRepository.save(userTransaction);
        }
        for (Membership member : selectedMembers){
            User otherUser = member.getUser();
            if (!otherUser.getId().equals(currentUser.getId())){
                Debt debt = new Debt();
                debt.setDebtor(expense ? otherUser : currentUser);
                debt.setCreditor(expense ? currentUser : otherUser);
                debt.setGroup(group);
                debt.setAmount(amountPerUser);
                debt.setTitle(transactionDTO.getTitle());
                debtRepository.save(debt);
            }
        }
        // Po pętli for (tworzącej długi)
        sendExpenseNotifications(transactionDTO, group, currentUser, selectedMembers, amountPerUser);
    }

    private List<Membership> selectParticipants(
            GroupTransactionDTO transactionDTO,
            List<Membership> members,
            User currentUser) {
        List<Long> selectedUserIds = transactionDTO.getSelectedUserIds();
        if (selectedUserIds == null || selectedUserIds.isEmpty()) {
            return members;
        }
        Set<Long> uniqueSelectedUserIds = new HashSet<>(selectedUserIds);
        List<Membership> selectedMembers = members.stream()
                .filter(membership -> uniqueSelectedUserIds.contains(membership.getUser().getId()))
                .toList();
        if (selectedMembers.size() != uniqueSelectedUserIds.size()) {
            throw new IllegalStateException(
                    "Wszyscy wybrani uzytkownicy musza byc czlonkami grupy.");
        }
        boolean currentUserSelected = selectedMembers.stream()
                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
        if (!currentUserSelected) {
            throw new IllegalStateException(
                    "Aktualny uzytkownik musi byc uczestnikiem transakcji grupowej.");
        }
        if (selectedMembers.size() < 2) {
            throw new IllegalStateException("Transakcja grupowa wymaga co najmniej dwoch uczestnikow.");
        }
        return selectedMembers;
    }

    private void sendExpenseNotifications(GroupTransactionDTO dto, Group group, User currentUser,
                                          List<Membership> selectedMembers, double amountPerUser) {
        // Tylko wydatki (EXPENSE) wywołują powiadomienia
        if (!"EXPENSE".equals(dto.getType())) return;

        for (Membership member : selectedMembers) {
            User targetUser = member.getUser();
            // Pomijamy samego dodającego
            if (targetUser.getId().equals(currentUser.getId())) continue;

            String messageText = String.format("%s dodał wydatek \"%s\" w grupie %s. Twoja część: %.2f zł.",
                    currentUser.getEmail(), dto.getTitle(), group.getName(), amountPerUser);

            Map<String, Object> notification = Map.of(
                    "type", "GROUP_EXPENSE_ADDED",
                    "groupId", group.getId(),
                    "groupName", group.getName(),
                    "title", dto.getTitle(),
                    "amount", dto.getAmount(),
                    "userShare", amountPerUser,
                    "createdByEmail", currentUser.getEmail(),
                    "message", messageText
            );

            messagingTemplate.convertAndSendToUser(
                    targetUser.getEmail(),          // email jako identyfikator użytkownika
                    "/queue/group-notifications",   // kolejka, na którą nasłuchuje frontend
                    notification
            );
        }
    }

}
