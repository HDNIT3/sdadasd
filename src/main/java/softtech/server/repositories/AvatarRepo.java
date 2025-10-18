// File: src/main/java/softtech/server/repositories/AvatarRepo.java
// Mục đích: Repository thao tác bảng avatars (KHÔNG dùng Optional → có thể trả null).

package softtech.server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import softtech.server.models.Avatar;

@Repository
public interface AvatarRepo extends JpaRepository<Avatar, String> {
    Avatar findByAccountId(String accountId);
    Avatar findImageUrlByAccountId(String accountId);
}
