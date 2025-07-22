package com.codecrafters.ownredis.utils;

import lombok.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

public class Example {

    public static void main(String[] args) {
        User user = new User(1L, "xyz123", "xyz", "xyz@gmail.com");
        System.out.println("User: " + user);

        System.out.println("Class Name: "+UserMapper.userMapperInstance.getClass().getName());
        UserDTO userDTO = UserMapper.userMapperInstance.userToUserDTO(user);
        System.out.println("UserDTO: " + userDTO);
    }
}


record UserDTO(Long id, String username, String email) {
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
class User {
    private Long id;
    private String username;
    private String password;
    private String email;
}

@Mapper(componentModel = "spring")
interface UserMapper {
    UserMapper userMapperInstance= Mappers.getMapper(UserMapper.class);
    UserDTO userToUserDTO(User user);
    User userDtoToUser(UserDTO userDTO);
}

