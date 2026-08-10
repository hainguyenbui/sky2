package com.example.spyfall.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataMember {

    int id;

    String role;

    String ipData;

    String description;

    String total;

    String nameMember;

    // Số thứ tự nhân vật
    String idPlayGame;

    @Builder.Default
    int protectedSkill = 0;

    @Builder.Default
    int killSkill = 0;

    @Builder.Default
    boolean superProtectedSkill = false;

    @Builder.Default
    boolean disabledSkill = false;

    @Builder.Default
    boolean isDead = false;

    @Builder.Default
    boolean inspectSkill = false;

    String detailShow;

    /**
     * 1 cupid
     */
    @Builder.Default
    int lifeLink = 0;
    List<Integer> lifeLinkIds;

    /**
     * 1 thợ săn
     * 2 nhân bản
     * 3 đuổi khòi làng
     * 4 tập sự
     * 5 người bệnh
     * 6 bị nguyền
     * 7 silent
     */
    @Builder.Default
    int connectSkill = 0;

    public DataMember(Integer id, String role) {
        this.id = id;
        this.role = role;
    }

    public DataMember(int id, String role, String description) {
        this.id = id;
        this.role = role;
        this.description =description;
    }

    public DataMember(DataMember dataMember, Integer total) {
        this.id = dataMember.getId();
        this.role = dataMember.getRole();
        this.description = dataMember.getDescription();
        this.total = String.valueOf(total);
    }

    public void decreaseKillSkill() {
        killSkill--;
    }

    public void decreaseProtectedSkill() {
        protectedSkill--;
    }
}
