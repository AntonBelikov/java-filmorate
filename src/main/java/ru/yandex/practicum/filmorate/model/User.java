package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private int id;
    @NonNull
    private String email;
    @NonNull
    private String login;
    @NonNull
    private String name;
    @NonNull
    private LocalDate birthday;
    private final Set<Integer> friends = new HashSet<>();

    public User(@NonNull String email, @NonNull String login, @NonNull String name, @NonNull LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public User(@NonNull String email, @NonNull String login, @NonNull LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
    }
}
