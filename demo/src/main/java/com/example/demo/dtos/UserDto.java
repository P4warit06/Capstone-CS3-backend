package com.example.demo.dtos;

import com.example.demo.entity.User;

public class UserDto {
    private Integer userId;
    private String email;
    private String studentId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String role;

    public UserDto() {}

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.userId = user.getId();
        dto.email = user.getEmail();
        dto.studentId = user.getStudentId();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.displayName = user.getDisplayName();
        dto.role = user.getRole().name();
        return dto;
    }

    public Integer getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getStudentId() { return studentId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getDisplayName() { return displayName; }
    public String getRole() { return role; }
}
