package com.nadir.userAuth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

 

@Entity
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @OneToOne
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private User user;

    private LocalDateTime expiryDate;
    
    
    public VerificationToken(String token, User user, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }


    
    
     public VerificationToken(){};


	public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

    // Getters, setters, constructors...
    
    
}

  