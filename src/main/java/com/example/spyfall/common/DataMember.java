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

    int protectedSkill = 0;

    int killSkill = 0;

    boolean superProtectedSkill = false;

    boolean disabledSkill = false;

    boolean isDead = false;

    /**
     * 1 cupid
     */
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
