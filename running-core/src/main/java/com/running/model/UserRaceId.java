package com.running.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRaceId implements Serializable {

    private Long user;
    private Long race;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRaceId)) return false;
        UserRaceId that = (UserRaceId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(race, that.race);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, race);
    }

}
