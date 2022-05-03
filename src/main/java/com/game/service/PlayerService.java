package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.List;
import java.util.Map;

public interface PlayerService {

    List<Player> getPlayers(String name, String title, Race race,
                            Profession profession, Long after, Long before,
                            Boolean banned, Integer minExperience, Integer maxExperience,
                            Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize);

    Integer getCount(String name, String title, Race race,
                     Profession profession, Long after, Long before,
                     Boolean banned, Integer minExperience, Integer maxExperience,
                     Integer minLevel, Integer maxLevel);

    Player createPlayer(Map<String, String> params);

    Player getPlayerById(Long id);

    Boolean deleteById(Long id);

    Player updatePlayerById(Long id, Map<String, String> params);
}
