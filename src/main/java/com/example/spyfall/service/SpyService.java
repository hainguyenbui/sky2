package com.example.spyfall.service;

import com.example.spyfall.common.Spy2;
import com.example.spyfall.util.CommonUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.example.spyfall.util.Constant.QR_SPY;

@Service
public class SpyService {

    private static final Set<Integer> DEFAULT_UNCHECKED_LOCATION_IDS = new LinkedHashSet<>(
            Arrays.asList(7, 16, 23, 24, 27, 29, 30, 32)
    );

    @Getter
    private List<Spy2> keywords = new ArrayList<>();
    private List<Spy2> dataWillPlay = new ArrayList<>();
    @Getter
    private List<Spy2> dataPlayer = new ArrayList<>();
    private List<Spy2> listItemOfSpy = new ArrayList<>();
    @Getter
    private List<Spy2> removed = new ArrayList<>();
    @Getter
    private final String image = QR_SPY;
    @Getter
    private int numberGamePlay = 0;
    private final Set<Integer> selectedKeywordIds = new LinkedHashSet<>();

    @Autowired
    DataInputService dataInputService;
    @Autowired
    SpyCommService spyCommService;

    /**
     * Get or assign a member role for the given IP.
     * Returns null if game not set up yet.
     * Returns the location/role string.
     */
    public String getOrAssignMember(String deviceId, String name) {
        if (!dataPlayer.isEmpty()) {
            for (Spy2 dataMember : dataPlayer) {
                if (dataMember.getIpConfig().equals(deviceId)) {
                    if (StringUtils.hasText(name) && !Objects.equals(dataMember.getUserName(), name)) {
                        dataMember.setUserName(name);
                        spyCommService.upsertPlayer(SpyCommService.GAME_SPY, dataMember);
                    }
                    return dataMember.getKeyword();
                }
            }
        }
        if (dataWillPlay.isEmpty()) {
            if (numberGamePlay >0) {
                return "";
            }
            return null; // not set up
        }
        Collections.shuffle(dataWillPlay);
        Integer randomSpy = CommonUtils.addToList(deviceId, dataWillPlay);
        if (randomSpy == null) return null;
        Spy2 yourLocation = dataWillPlay.get(randomSpy);
        yourLocation.setIpConfig(deviceId);
        yourLocation.setUserName(name);
        dataPlayer.add(yourLocation);
        dataWillPlay.remove(randomSpy.intValue());
        spyCommService.upsertPlayer(SpyCommService.GAME_SPY, yourLocation);
        return yourLocation.getKeyword();
    }

    public boolean isGameSetup() {
        return !dataWillPlay.isEmpty() || !dataPlayer.isEmpty();
    }

    public void setupGame(int total, Integer spy, Integer whiteHat) {
        dataPlayer.clear();
        dataWillPlay.clear();
        listItemOfSpy.clear();
        removed.clear();
        numberGamePlay++;
        spyCommService.resetGame(SpyCommService.GAME_SPY);

        ensureKeywordsLoaded();

        Random random = new Random();
        listItemOfSpy = getActiveKeywords();
        if (listItemOfSpy.size() < 2) {
            throw new IllegalStateException("Cần chọn ít nhất 2 địa điểm để tạo ván.");
        }
        int indexMember = random.nextInt(listItemOfSpy.size());
        Spy2 dataNotSpy = listItemOfSpy.get(indexMember);
        listItemOfSpy.remove(indexMember);

        int indexSpy = random.nextInt(listItemOfSpy.size());
        Spy2 dataSpy = listItemOfSpy.get(indexSpy);

        int totalWhiteHatAndSpy = whiteHat + spy;
        int cnt = 1;
        for (int i = 0; i < (total - totalWhiteHatAndSpy); i++) {
            dataWillPlay.add(createDM(cnt++, dataNotSpy.getKeyword(), "Dân Thường"));
        }
        for (int i = 0; i < spy; i++) {
            dataWillPlay.add(createDM(cnt++, dataSpy.getKeyword(), "Spy"));
        }
        for (int i = 0; i < whiteHat; i++) {
            dataWillPlay.add(createDM(cnt++, "Spy đó nhìn nhìn cái gì", "Mũ Trắng"));
        }
    }

    public synchronized List<Map<String, Object>> getLocationOptions() {
        ensureKeywordsLoaded();
        List<Map<String, Object>> options = new ArrayList<>();
        for (Spy2 keyword : keywords) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", keyword.getId());
            item.put("name", keyword.getKeyword());
            item.put("selected", selectedKeywordIds.contains(keyword.getId()));
            options.add(item);
        }
        return options;
    }

    public synchronized void updateSelectedLocationsFromCsv(String selectedIdsCsv) {
        ensureKeywordsLoaded();
        if (!StringUtils.hasText(selectedIdsCsv)) {
            selectedKeywordIds.clear();
            return;
        }
        Set<Integer> next = new LinkedHashSet<>();
        String[] chunks = selectedIdsCsv.split(",");
        for (String chunk : chunks) {
            String value = chunk.trim();
            if (!StringUtils.hasText(value)) {
                continue;
            }
            try {
                next.add(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
            }
        }
        selectedKeywordIds.clear();
        Set<Integer> validIds = keywords.stream().map(Spy2::getId).collect(LinkedHashSet::new, Set::add, Set::addAll);
        next.stream().filter(validIds::contains).forEach(selectedKeywordIds::add);
    }

    private synchronized void ensureKeywordsLoaded() {
        if (!ObjectUtils.isEmpty(keywords)) {
            ensureDefaultSelectionInitialized();
            return;
        }
        String[] datas = dataInputService.valueDataSpy().split(",");
        for (String dataItem : datas) {
            String trimmed = dataItem.trim();
            boolean exist = keywords.stream().anyMatch(item -> item.getKeyword().equals(trimmed));
            if (!exist) {
                int newId = keywords.size() + 1;
                keywords.add(createDM(newId, trimmed, null));
            }
        }
        ensureDefaultSelectionInitialized();
    }

    private void ensureDefaultSelectionInitialized() {
        if (!selectedKeywordIds.isEmpty()) {
            return;
        }
        for (Spy2 keyword : keywords) {
            if (!DEFAULT_UNCHECKED_LOCATION_IDS.contains(keyword.getId())) {
                selectedKeywordIds.add(keyword.getId());
            }
        }
    }

    private List<Spy2> getActiveKeywords() {
        return keywords.stream()
                .filter(item -> selectedKeywordIds.contains(item.getId()))
                .map(item -> createDM(item.getId(), item.getKeyword(), item.getRole()))
                .collect(ArrayList::new, List::add, List::addAll);
    }

    public boolean deleteItem(int id) {
        for (Spy2 item : keywords) {
            if (Objects.equals(item.getId(), id)) {
                keywords.remove(item);
                return true;
            }
        }
        return false;
    }

    public void addItem(String name) {
        int newId = keywords.stream()
                .mapToInt(Spy2::getId)
                .max().orElse(0) + 1;
        keywords.add(createDM(newId, name, null));
    }

    public Spy2 removePlayer(int id) {
        for (Spy2 player : dataPlayer) {
            if (Objects.equals(player.getId(), id)) {
                player.setRemove(true);
                if (!removed.contains(player)) {
                    removed.add(player);
                }
                spyCommService.markRemoved(SpyCommService.GAME_SPY, id);
                return player;
            }
        }
        return null;
    }

    private Spy2 createDM(int id, String location, String role) {
        return Spy2.builder().id(id).keyword(location).role(role).build();
    }

}
