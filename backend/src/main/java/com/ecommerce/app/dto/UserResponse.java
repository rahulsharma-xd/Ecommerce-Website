package com.ecommerce.app.dto;

import java.time.LocalDateTime;

public class UserResponse {
     private Long id;
      private String username;
      private String email;
      private String role;
      private boolean active;
      private LocalDateTime createdAt;
      private LocalDateTime updatedAt;

      public UserResponse(Long id, String username, String email, String role, 
                         boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
          this.id = id;
          this.username = username;
          this.email = email;
          this.role = role;
          this.active = active;
          this.createdAt = createdAt;
          this.updatedAt = updatedAt;
      }

      // Getters
      public Long getId() { return id; }
      public String getUsername() { return username; }
      public String getEmail() { return email; }
      public String getRole() { return role; }
      public boolean isActive() { return active; }
      public LocalDateTime getCreatedAt() { return createdAt; }
      public LocalDateTime getUpdatedAt() { return updatedAt; }

      
}
