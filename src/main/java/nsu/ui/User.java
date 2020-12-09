package nsu.ui;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;

public class User {

    private Long id;

    private static User instance;

    @NotEmpty(message = "Email обязателен для заполнения.")
    private String email;

    @NotEmpty(message = "Пароль обязателен для заполнения.")
    private String password;

    private String name;

    private String age;

    private String height;

    public static synchronized User getInstance() {
        if (instance == null) {
            System.out.println("instance = 0");
            instance = new User();
        }
        System.out.println(instance.getId());
        return instance;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return this.age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getHeight() {
        return this.height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setInstance(User user) {
        instance = user;
    }


}
