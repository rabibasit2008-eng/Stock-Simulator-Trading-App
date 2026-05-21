package com.StockSim.Storage;
import com.StockSim.Model.User;
import java.util.Optional;

public class UserStore {
    private static final String SEP = "|";

    // File format per line: username|passwordHash
    public void save(User user) {
        String line = user.getId() + SEP + user.getUsername() + SEP + user.getPasswordHash();
        FileHelper.appendLine(FileHelper.usersFile(), line);
    }
    public Optional<User> findByUsername(String username) {
        for (String line : FileHelper.readLines(FileHelper.usersFile())) {
            String[] parts = line.split("\\|", 3);
            if (parts.length == 3 && parts[1].equals(username)) {
                // New format: id|username|passwordHash
                return Optional.of(new User(parts[0], parts[1], parts[2]));
            } else if (parts.length == 2 && parts[0].equals(username)) {
                // Legacy format: username|passwordHash — derive a stable ID
                String stableId = java.util.UUID.nameUUIDFromBytes(
                        parts[0].getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
                return Optional.of(new User(stableId, parts[0], parts[1]));
            }
        }
        return Optional.empty();
    }
    public boolean exists(String username) {
        return findByUsername(username).isPresent();
    }
}