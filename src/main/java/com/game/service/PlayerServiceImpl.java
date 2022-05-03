package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PlayerServiceImpl implements PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public List<Player> getPlayers(String name, String title, Race race,
                                   Profession profession, Long after, Long before,
                                   Boolean banned, Integer minExperience, Integer maxExperience,
                                   Integer minLevel, Integer maxLevel, PlayerOrder order,
                                   Integer pageNumber, Integer pageSize) {
        List<Player> list = new ArrayList<>();
        playerRepository.findAll(Sort.by(order.getFieldName())).forEach(list::add);
        List<Player> filteredList = filterList(list, name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        PagedListHolder<Player> pagedListHolder = new PagedListHolder<>(filteredList);
        pagedListHolder.setPage(pageNumber);
        pagedListHolder.setPageSize(pageSize);
        return pagedListHolder.getPageList();
    }

    public Integer getCount(String name, String title, Race race,
                            Profession profession, Long after, Long before,
                            Boolean banned, Integer minExperience, Integer maxExperience,
                            Integer minLevel, Integer maxLevel) {
        List<Player> list = new ArrayList<>();
        playerRepository.findAll().forEach(list::add);
        return filterList(list, name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel).size();
    }

    public List<Player> filterList(List<Player> inputList, String name, String title, Race race,
                                   Profession profession, Long after, Long before,
                                   Boolean banned, Integer minExperience, Integer maxExperience,
                                   Integer minLevel, Integer maxLevel) {
        List<Player> filteredList = new ArrayList<>(inputList);
        Stream<Player> stream = filteredList.stream();
        if (name != null) stream = stream.filter(w -> w.getName().contains(name));
        if (title != null) stream = stream.filter(w -> w.getTitle().contains(title));
        if (race != null) stream = stream.filter(w -> w.getRace() == race);
        if (profession != null) stream = stream.filter(w -> w.getProfession() == profession);
        if (after > 0) stream = stream.filter(w -> w.getBirthday().getTime() >= after);
        if (before > 0) stream = stream.filter(w -> w.getBirthday().getTime() <= before);
        if (banned != null) stream = stream.filter(w -> w.getBanned().equals(banned));
        if (minExperience > 0) stream = stream.filter(w -> w.getExperience() >= minExperience);
        if (maxExperience > 0) stream = stream.filter(w -> w.getExperience() <= maxExperience);
        if (minLevel > 0) stream = stream.filter(w -> w.getLevel() >= minLevel);
        if (maxLevel > 0) stream = stream.filter(w -> w.getLevel() <= maxLevel);
        return stream.collect(Collectors.toList());
    }

    public Player createPlayer(Map<String, String> params) {
        if (checkDataParams(params)) {
            Player newPlayer = new Player();
            newPlayer.setName(params.get("name"));
            newPlayer.setTitle(params.get("title"));
            newPlayer.setRace(Race.valueOf(params.get("race")));
            newPlayer.setProfession(Profession.valueOf(params.get("profession")));
            newPlayer.setBirthday(new Date(Long.parseLong(params.get("birthday"))));
            newPlayer.setBanned(Boolean.parseBoolean(params.get("banned")));
            int experience = Integer.parseInt(params.get("experience"));
            newPlayer.setExperience(experience);
            int level = level(experience);
            newPlayer.setLevel(level);
            int untilNextLevel = untilNextLevel(level, experience);
            newPlayer.setUntilNextLevel(untilNextLevel);
            return playerRepository.save(newPlayer);
        }
        return null;
    }

    public Boolean checkDataParams(Map<String, String> params) {
        if (params.size() == 0) return false;
        if (params.get("name") == null || params.get("name").equals("") || params.get("name").length() > 12) return false;
        if (params.get("title") == null || params.get("title").length() > 30) return false;
        if (params.get("birthday") == null || Long.parseLong(params.get("birthday")) < 0) return false;
        if (params.get("experience") == null || Integer.parseInt(params.get("experience")) < 0 || Integer.parseInt(params.get("experience")) > 10000000) return false;
        Date date = new Date(Long.parseLong(params.get("birthday")));
        if (date.getYear() + 1900 < 2000 || date.getYear() + 1900 > 3000) return false;
        return true;
    }

    private int level(int exp) {
        return (int) (Math.sqrt(2500 + 200 * exp) - 50) / 100;
    }

    private int untilNextLevel(int level, int exp) {
        return 50 * (level + 1) * (level + 2) - exp;
    }

    @Override
    public Player getPlayerById(Long id) {
        Player player = null;
        Optional<Player> optionalPlayer = playerRepository.findById(id);
        if (optionalPlayer.isPresent()) player = optionalPlayer.get();
        return player;
    }

    public Boolean deleteById(Long id) {
        Boolean isExists = false;
        if (playerRepository.existsById(id)) {
            isExists = true;
            playerRepository.deleteById(id);
        }
        return isExists;
    }

    public Player updatePlayerById(Long id, Map<String, String> params) {
        if (playerRepository.existsById(id)) {
            Player player = getPlayerById(id);
            if (params.get("name") != null) {
                if (params.get("name").equals("") || params.get("name").length() > 12) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                else player.setName(params.get("name"));
            }
            if (params.get("title") != null) {
                if (params.get("title").length() > 30) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                else player.setTitle(params.get("title"));
            }
            if (params.get("race") != null) {
                player.setRace(Race.valueOf(params.get("race")));
            }
            if (params.get("profession") != null) {
                player.setProfession(Profession.valueOf(params.get("profession")));
            }
            if (params.get("birthday") != null) {
                Long birthday = Long.parseLong(params.get("birthday"));
                if (birthday < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                Date date = new Date(birthday);
                if (date.getYear() + 1900 < 2000 || date.getYear() + 1900 > 3000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                else player.setBirthday(date);
            }
            if (params.get("banned") != null) {
                player.setBanned(Boolean.parseBoolean(params.get("banned")));
            }
            if (params.get("experience") != null) {
                Integer experience = Integer.parseInt(params.get("experience"));
                if (experience < 0 || experience > 10000000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                player.setExperience(experience);
                int level = level(experience);
                player.setLevel(level);
                player.setUntilNextLevel(untilNextLevel(level, experience));
            }
            return playerRepository.save(player);
        }
        return null;
    }
}
