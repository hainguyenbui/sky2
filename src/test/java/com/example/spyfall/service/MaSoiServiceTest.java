package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.DayActionDto;
import com.example.spyfall.common.GameSessionState;
import com.example.spyfall.common.NightActionDto;
import com.example.spyfall.common.NightGuideStep;
import com.example.spyfall.common.RoundHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MaSoiServiceTest {

    private MaSoiService service;

    @BeforeEach
    void setUp() {
        service = new MaSoiService();
        service.dataInputService = new DataInputService();
    }

    @Test
    void cloneLinksAreClearedBetweenGames() throws Exception {
        loadGame(2, 42);
        DataMember firstAlpha = assign(2, "device-a", "Alpha A");
        DataMember firstClone = assign(42, "device-b", "Clone B");

        service.processNight(List.of(action(firstClone, firstAlpha, "conn", "2")));
        service.endGame();

        loadGame(2, 42);
        DataMember secondAlpha = assign(2, "device-b", "Alpha B");
        DataMember secondClone = assign(42, "device-a", "Clone A");

        service.processDay(List.of(secondClone.getIpData()));

        assertThat(secondAlpha.getId()).isEqualTo(2);
        assertThat(secondAlpha.getRole()).isEqualTo("Sói đầu đàn");
    }

    @Test
    void cloneCopiesTheExactRoleAndAllCapabilities() throws Exception {
        loadGame(2, 42);
        DataMember alpha = assign(2, "alpha", "Alpha");
        DataMember clone = assign(42, "clone", "Clone");

        service.processNight(List.of(action(clone, alpha, "conn", "2")));
        service.processDay(List.of(alpha.getIpData()));

        assertThat(clone.getId()).isEqualTo(2);
        assertThat(clone.getRole()).isEqualTo("Sói đầu đàn");
        assertThat(clone.getDetailShow()).isEqualTo("Sói đầu đàn");
        assertThat(clone.getKillSkill()).isEqualTo(9999);
        assertThat(service.getAdminCurrentHistory())
                .flatExtracting(RoundHistory::events)
                .anyMatch(event -> event.contains("đã trở thành Sói đầu đàn"));
    }

    @Test
    void curseAndSeerColumnsAreCreatedWithoutAngel() throws Exception {
        loadGame(3, 30, 32);
        assign(3, "curse", "Curse");
        assign(30, "villager", "Villager");
        assign(32, "seer", "Seer");

        List<Map<String, Object>> columns = columns();

        assertThat(columns).anySatisfy(column -> {
            assertThat(column.get("roleId")).isEqualTo(3);
            assertThat(column.get("hasRecruit")).isEqualTo(true);
        });
        assertThat(columns).anySatisfy(column -> {
            assertThat(column.get("roleId")).isEqualTo(32);
            assertThat(column.get("hasInspect")).isEqualTo(true);
        });
    }

    @Test
    void unclaimedRolesDoNotCreateAdminActionColumns() throws Exception {
        loadGame(3, 32);
        assign(32, "seer", "Seer");

        assertThat(columns()).singleElement().satisfies(column ->
                assertThat(column.get("roleId")).isEqualTo(32));
    }

    @Test
    void successfulCurseCreatesABasicWolfAndConsumesTheAbility() throws Exception {
        loadGame(3, 30, 32);
        DataMember cursedWolf = assign(3, "curse", "Curse");
        DataMember villager = assign(30, "villager", "Villager");
        DataMember seer = assign(32, "seer", "Seer");

        String result = service.processNight(List.of(action(cursedWolf, seer, "recruit", null)));

        assertThat(result).doesNotStartWith("ERROR");
        assertThat(seer.getId()).isEqualTo(1);
        assertThat(seer.getRole()).isEqualTo("Sói");
        assertThat(seer.isInspectSkill()).isFalse();
        assertThat(seer.getConnectSkill()).isZero();
        assertThat(columns()).noneMatch(column -> Boolean.TRUE.equals(column.get("hasRecruit")));

        String secondAttempt = service.processNight(List.of(action(cursedWolf, villager, "recruit", null)));
        assertThat(secondAttempt).startsWith("ERROR Lời nguyền đã được sử dụng");
        assertThat(villager.getId()).isEqualTo(30);
    }

    @Test
    void invalidCurseDoesNotConsumeTheAbility() throws Exception {
        loadGame(2, 3, 30);
        DataMember alpha = assign(2, "alpha", "Alpha");
        DataMember cursedWolf = assign(3, "curse", "Curse");
        DataMember villager = assign(30, "villager", "Villager");

        String invalid = service.processNight(List.of(action(cursedWolf, alpha, "recruit", null)));
        String valid = service.processNight(List.of(action(cursedWolf, villager, "recruit", null)));

        assertThat(invalid).startsWith("ERROR Không thể nguyền người thuộc phe Sói");
        assertThat(valid).doesNotStartWith("ERROR");
        assertThat(villager.getId()).isEqualTo(1);
    }

    @Test
    void seerNeedsTwoInspectionsToDetectDoubleCheckWolf() throws Exception {
        loadGame(4, 30, 32);
        DataMember doubleCheckWolf = assign(4, "double", "Double Wolf");
        assign(30, "villager", "Villager");
        DataMember seer = assign(32, "seer", "Seer");

        String first = service.processNight(List.of(action(seer, doubleCheckWolf, "inspect", null)));
        String second = service.processNight(List.of(action(seer, doubleCheckWolf, "inspect", null)));

        assertThat(first).contains("Không phải Sói/Sát thủ");
        assertThat(second).contains("Là Sói/Sát thủ");
        assertThat(service.getPlayerCurrentHistory())
                .flatExtracting(RoundHistory::events)
                .noneMatch(event -> event.contains("Tiên tri"));
    }

    @Test
    void seerTreatsTraitorAsNegativeAndAssassinAsPositive() throws Exception {
        loadGame(15, 20, 32);
        DataMember traitor = assign(15, "traitor", "Traitor");
        DataMember assassin = assign(20, "assassin", "Assassin");
        DataMember seer = assign(32, "seer", "Seer");

        String traitorResult = service.processNight(List.of(action(seer, traitor, "inspect", null)));
        String assassinResult = service.processNight(List.of(action(seer, assassin, "inspect", null)));

        assertThat(traitorResult).contains("Không phải Sói/Sát thủ");
        assertThat(assassinResult).contains("Là Sói/Sát thủ");
    }

    @Test
    void touchingInfectedWolfKillsSeerButKeepsResultAdminOnly() throws Exception {
        loadGame(5, 32);
        DataMember infectedWolf = assign(5, "infected", "Infected Wolf");
        DataMember seer = assign(32, "seer", "Seer");

        service.processNight(List.of(action(seer, infectedWolf, "inspect", null)));

        assertThat(seer.isDead()).isTrue();
        assertThat(service.getAdminCurrentHistory())
                .flatExtracting(RoundHistory::events)
                .anyMatch(event -> event.contains("Tiên tri Seer soi Infected Wolf"));
        assertThat(service.getPlayerCurrentHistory())
                .flatExtracting(RoundHistory::events)
                .noneMatch(event -> event.contains("soi Infected Wolf"));
    }

    @Test
    void adminHistoryIsArchivedInMemoryAndCurrentTimelineIsReset() throws Exception {
        loadGame(30, 32);
        DataMember villager = assign(30, "villager", "Villager");
        DataMember seer = assign(32, "seer", "Seer");
        service.processNight(List.of(action(seer, villager, "inspect", null)));

        service.endGame();

        assertThat(service.getAdminCurrentHistory()).isEmpty();
        Map<String, Object> managementData = service.getGameManagementData();
        assertThat(managementData.get("gameActive")).isEqualTo(false);
        assertThat((List<?>) managementData.get("archivedAdminGames")).hasSize(1);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> playerGames =
                (List<Map<String, Object>>) service.getGameHistoryData().get("games");
        assertThat(playerGames).singleElement().satisfies(game -> {
            @SuppressWarnings("unchecked")
            List<RoundHistory> playerRounds = (List<RoundHistory>) game.get("details");
            assertThat(playerRounds)
                    .flatExtracting(RoundHistory::events)
                    .noneMatch(event -> event.contains("Tiên tri"));
        });
    }

    @Test
    void discardingSetupDoesNotArchiveOrAdvanceTheGameNumber() throws Exception {
        loadGame(2, 30);
        long discardedSessionId = service.getCurrentGameSessionId();
        DataMember firstRole = service.getOrAssignRole("same-device", "Người chơi");

        assertThat(firstRole).isNotNull();
        assertThat(service.canResetSetup()).isTrue();
        assertThat(service.isGameStarted()).isFalse();
        assertThat(service.endGame()).startsWith("ERROR Ván chưa bắt đầu");

        assertThat(service.discardSetup()).startsWith("OK");

        GameSessionState resetting = service.getGameSessionState(discardedSessionId);
        assertThat(resetting.lifecycle()).isEqualTo("RESETTING");
        assertThat(resetting.active()).isFalse();
        assertThat(resetting.requestedSessionDiscarded()).isTrue();
        assertThat(resetting.requestedSessionFinished()).isFalse();
        assertThat(resetting.replacementReady()).isFalse();
        assertThat(service.getPls()).isEmpty();
        assertThat(service.getGameNumber()).isEqualTo(1);
        assertThat((List<?>) service.getGameHistoryData().get("games")).isEmpty();

        loadGame(3, 30);
        DataMember replacementRole = service.getOrAssignRole("same-device", "Người chơi");
        GameSessionState replaced = service.getGameSessionState(discardedSessionId);

        assertThat(replacementRole).isNotNull().isNotSameAs(firstRole);
        assertThat(service.getCurrentGameSessionId()).isGreaterThan(discardedSessionId);
        assertThat(replaced.lifecycle()).isEqualTo("SETUP");
        assertThat(replaced.active()).isTrue();
        assertThat(replaced.requestedSessionDiscarded()).isTrue();
        assertThat(replaced.replacementReady()).isTrue();
        assertThat(service.getGameNumber()).isEqualTo(1);
        assertThat((List<?>) service.getGameHistoryData().get("games")).isEmpty();
    }

    @Test
    void setupCannotBeDiscardedAfterTheFirstSuccessfulNight() throws Exception {
        loadGame(2, 30);
        long sessionId = service.getCurrentGameSessionId();

        assertThat(service.processNight(null)).startsWith("ERROR");
        assertThat(service.canResetSetup()).isTrue();
        assertThat(service.processNight(List.of())).doesNotStartWith("ERROR");
        assertThat(service.canResetSetup()).isFalse();
        assertThat(service.isGameStarted()).isTrue();
        assertThat(service.discardSetup()).startsWith("ERROR Ván đã bắt đầu");

        assertThat(service.endGame()).startsWith("End Game");

        GameSessionState finished = service.getGameSessionState(sessionId);
        assertThat(finished.lifecycle()).isEqualTo("FINISHED");
        assertThat(finished.active()).isFalse();
        assertThat(finished.requestedSessionDiscarded()).isFalse();
        assertThat(finished.requestedSessionFinished()).isTrue();
        assertThat((List<?>) service.getGameHistoryData().get("games")).hasSize(1);
    }

    @Test
    void nightGuideUsesDependencyOrderAndMovesOldWitchToDay() throws Exception {
        int[] roleIds = {2, 3, 20, 30, 31, 32, 33, 34, 38, 42, 43, 44, 45, 46};
        loadGame(roleIds);
        for (int roleId : roleIds) assign(roleId, "device-" + roleId, "Role " + roleId);

        Map<String, Object> management = service.getGameManagementData();
        @SuppressWarnings("unchecked")
        List<NightGuideStep> steps = (List<NightGuideStep>) management.get("nightGuideSteps");

        assertThat(steps).extracting(NightGuideStep::key).containsExactly(
                "silence", "angel", "clone", "hunter", "guard", "wolves",
                "witch", "assassin", "dictator", "bomb", "seer");
        assertThat(columns()).noneMatch(column -> column.get("roleId").equals(45));
        assertThat(management.get("oldWitchAction")).isNull();
    }

    @Test
    void silencerCannotTargetSelfOrAngel() throws Exception {
        loadGame(30, 38, 44);
        DataMember villager = assign(30, "villager", "Villager");
        DataMember silencer = assign(38, "silencer", "Silencer");
        DataMember angel = assign(44, "angel", "Angel");

        assertThat(service.processNight(List.of(action(silencer, silencer, "conn", "7"))))
                .startsWith("ERROR Câm lặng không thể tự chọn mình");
        assertThat(service.processNight(List.of(action(silencer, angel, "conn", "7"))))
                .startsWith("ERROR Thiên thần không thể bị câm lặng");
        assertThat(service.processNight(List.of(action(silencer, villager, "conn", "7"))))
                .doesNotStartWith("ERROR");
    }

    @Test
    void witchCanOnlySaveTheWolfBiteTarget() throws Exception {
        loadGame(2, 30, 31, 32);
        DataMember wolf = assign(2, "wolf", "Wolf");
        DataMember victim = assign(30, "victim", "Victim");
        DataMember witch = assign(31, "witch", "Witch");
        DataMember other = assign(32, "other", "Other");

        String invalid = service.processNight(List.of(
                action(wolf, victim, "soi", null),
                action(witch, other, "prot", null)));
        String valid = service.processNight(List.of(
                action(wolf, victim, "soi", null),
                action(witch, victim, "prot", null)));

        assertThat(invalid).startsWith("ERROR Phù thủy chỉ được cứu người bị Sói cắn");
        assertThat(valid).doesNotStartWith("ERROR");
        assertThat(victim.isDead()).isFalse();
        assertThat(witch.getProtectedSkill()).isZero();
    }

    @Test
    void oldWitchActsOnceDuringDayAndVoteCanBeSkipped() throws Exception {
        loadGame(30, 32, 45);
        DataMember villager = assign(30, "villager", "Villager");
        assign(32, "seer", "Seer");
        DataMember oldWitch = assign(45, "old-witch", "Old Witch");

        service.processNight(List.of());
        assertThat(service.getGameManagementData().get("dayPhaseActive")).isEqualTo(true);
        assertThat(service.getGameManagementData().get("oldWitchAction")).isNotNull();

        String first = service.processDayRole(new DayActionDto("oldWitch", oldWitch.getIpData(), villager.getIpData()));
        String second = service.processDayRole(new DayActionDto("oldWitch", oldWitch.getIpData(), "seer"));

        assertThat(first).contains("bị Phù thủy già đuổi khỏi làng");
        assertThat(second).startsWith("ERROR Phù thủy già đã hành động");
        assertThat(villager.isDead()).isTrue();
        assertThat(service.getGameManagementData().get("oldWitchAction")).isNull();

        assertThat(service.processDay(List.of())).contains("Không có ai bị loại qua biểu quyết");
        assertThat(service.getGameManagementData().get("dayPhaseActive")).isEqualTo(false);
        assertThat(service.getAdminCurrentHistory())
                .filteredOn(round -> round.label().equals("Ngày 1"))
                .singleElement()
                .satisfies(round -> assertThat(round.events()).hasSize(2));
    }

    private void loadGame(int... roleIds) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("total", String.valueOf(roleIds.length));
        params.put("checkCupid", "false");
        Arrays.stream(roleIds).forEach(roleId -> params.put(String.valueOf(roleId), "1"));
        assertThat(service.loadGame(params)).isEqualTo("OK load");
    }

    private DataMember assign(int roleId, String deviceId, String name) {
        DataMember player = service.getPls().stream()
                .filter(candidate -> candidate.getId() == roleId)
                .findFirst()
                .orElseThrow();
        player.setIpData(deviceId);
        player.setNameMember(name);
        return player;
    }

    private NightActionDto action(DataMember actor, DataMember target, String type, String connectionValue) {
        return new NightActionDto(
                target.getIpData(), target.getNameMember(), target.getId(), target.getRole(),
                actor.getId(), actor.getRole(), actor.getIpData(), type, connectionValue);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> columns() {
        return (List<Map<String, Object>>) service.getGameManagementData().get("columns");
    }
}
