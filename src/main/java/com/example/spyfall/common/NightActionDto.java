package com.example.spyfall.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NightActionDto {
    private String targetDeviceId;   // mục tiêu bị chọn
    private String targetName;
    private int targetRoleId;   // roleId của mục tiêu
    private String targetRoleName;   // tên vai trò của mục tiêu
    private int roleId;     // roleId của cột
    private String roleName;   // tên cột (tên vai trò thực hiện)
    private String deviceId;
    private String colType;    // soi | kill | prot | conn
    private String connValue;  // chỉ có khi colType=conn
}
