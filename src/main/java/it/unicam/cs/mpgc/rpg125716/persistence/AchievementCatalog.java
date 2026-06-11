package it.unicam.cs.mpgc.rpg125716.persistence;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import it.unicam.cs.mpgc.rpg125716.model.progression.Achievement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "achievements")
public class AchievementCatalog {
    @JacksonXmlProperty(localName = "achievement")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Achievement> achievements;

    public List<Achievement> getAchievements() {
        return achievements == null ? List.of() : List.copyOf(achievements);
    }
}
