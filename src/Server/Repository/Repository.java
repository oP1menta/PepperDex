package Server.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import Server.Pokemon.PokemonSpecies;
import Server.Database.Database;
import Server.Trainer.CharacterSheet;
import Server.Encounter.Area;
import Server.Encounter.AreaForm;
import Server.Encounter.AreaPool;
import Server.Encounter.Encounter;
import Server.Encounter.EncounterParticipant;
import Server.Encounter.EncounterPokemon;
import Server.Encounter.EncounterSpawn;
import Server.Evolution.Evolution;
import Server.Evolution.EvolutionCondition;
import Server.Injury.Injury;
import Server.Item.Item;
import Server.Master.Master;
import Server.PC.PC;
import Server.PC.PCBox;
import Server.Trainer.Player;
import Server.Trainer.Trainer;
import Server.Trainer.TrainerInventory;
import Server.Pokemon.Pokemon;
import Server.Pokemon.PokemonAttributePoints;
import Server.Pokemon.PokemonCapability;
import Server.Pokemon.PokemonCondition;
import Server.Pokemon.PokemonForm;
import Server.Pokemon.PokemonMove;
import Server.Pokemon.PokemonSkill;
import Server.Pokemon.PokemonSprite;
import Server.Pokemon.PokemonVitamin;
import Server.PokemonData.Ability;
import Server.PokemonData.Capability;
import Server.PokemonData.FormAbility;
import Server.PokemonData.FormCapability;
import Server.PokemonData.FormSkill;
import Server.PokemonData.Move;
import Server.PokemonData.Nature;
import Server.PokemonData.Skill;
import Server.Session.GameSession;
import Server.Session.SessionParticipant;
import Server.Trade.Trade;
import Server.Trade.TradeOffer;



public class Repository {

    private Connection connection;

    public Repository(Database database) {

        setConnection(database.getConnection());

    }

    //=================GETTERS===========

    public Connection getConnection() {

        return connection;

    }

    //===============SETTERS==============

    public void setConnection(Connection connection) {

        this.connection = connection;

    }

    //=================PLAYER METHODS===========

    public void createPlayer(Player player) {

        String sql =
            """
            INSERT INTO players (id, name)
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, player.getId());
            statement.setString(2, player.getName());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public Player findPlayerById(int id) {

        String sql =
            """
            SELECT id, name
            FROM players
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Player(
                        result.getInt("id"),
                        result.getString("name")
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
        return null;
    }

    public List<Player> findAllPlayers() {

        String sql =
            """
            SELECT id, name
            FROM players
            """;

        List<Player> players = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {
                players.add(
                    new Player(
                        result.getInt("id"),
                        result.getString("name")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public void updatePlayer(Player player) {

        String sql =
            """
            UPDATE players
            SET name = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, player.getName());
            statement.setInt(2, player.getId());
            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void deletePlayer(int id) {

        String sql =
            """
            DELETE FROM players
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  //=================POKEMON SPECIES METHODS===========

    public void createPokemonSpecies(PokemonSpecies pokemonSpecies) {

        String sql =
            """
            INSERT INTO pokemon_species (dex_number, name)
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonSpecies.getDexNumber());
            statement.setString(2, pokemonSpecies.getName());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public PokemonSpecies findPokemonSpeciesByDexNumber(int dexNumber) {

        String sql =
            """
            SELECT dex_number, name
            FROM pokemon_species
            WHERE dex_number = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, dexNumber);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonSpecies(
                        result.getInt("dex_number"),
                        result.getString("name")
                    );

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return null;

    }

    public List<PokemonSpecies> findAllPokemonSpecies() {

        String sql =
            """
            SELECT dex_number, name
            FROM pokemon_species
            """;

        List<PokemonSpecies> pokemonSpecies = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonSpecies.add(
                    new PokemonSpecies(
                        result.getInt("dex_number"),
                        result.getString("name")
                    )
                );

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return pokemonSpecies;

    }

    public void updatePokemonSpecies(PokemonSpecies pokemonSpecies) {

        String sql =
            """
            UPDATE pokemon_species
            SET name = ?
            WHERE dex_number = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, pokemonSpecies.getName());
            statement.setInt(2, pokemonSpecies.getDexNumber());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public void deletePokemonSpecies(int dexNumber) {

        String sql =
            """
            DELETE FROM pokemon_species
            WHERE dex_number = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, dexNumber);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }
    
  //=================POKEMON FORM METHODS===========

    public void createPokemonForm(PokemonForm pokemonForm) {

        String sql =
            """
            INSERT INTO pokemon_forms (
                id,
                species_id,
                name,
                type1,
                type2,
                base_hp,
                base_attack,
                base_defense,
                base_sp_attack,
                base_sp_defense,
                base_speed,
                cry_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonForm.getId());
            statement.setInt(2, pokemonForm.getSpeciesId());
            statement.setString(3, pokemonForm.getName());
            statement.setString(4, pokemonForm.getType1());
            statement.setString(5, pokemonForm.getType2());
            statement.setInt(6, pokemonForm.getBaseHp());
            statement.setInt(7, pokemonForm.getBaseAttack());
            statement.setInt(8, pokemonForm.getBaseDefense());
            statement.setInt(9, pokemonForm.getBaseSpAttack());
            statement.setInt(10, pokemonForm.getBaseSpDefense());
            statement.setInt(11, pokemonForm.getBaseSpeed());
            statement.setInt(12, pokemonForm.getCryId());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public PokemonForm findPokemonFormById(int id) {

        String sql =
            """
            SELECT
                id,
                species_id,
                name,
                type1,
                type2,
                base_hp,
                base_attack,
                base_defense,
                base_sp_attack,
                base_sp_defense,
                base_speed,
                cry_id
            FROM pokemon_forms
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonForm(
                        result.getInt("id"),
                        result.getInt("species_id"),
                        result.getString("name"),
                        result.getString("type1"),
                        result.getString("type2"),
                        result.getInt("base_hp"),
                        result.getInt("base_attack"),
                        result.getInt("base_defense"),
                        result.getInt("base_sp_attack"),
                        result.getInt("base_sp_defense"),
                        result.getInt("base_speed"),
                        result.getInt("cry_id")
                    );

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return null;

    }

    public List<PokemonForm> findAllPokemonForms() {

        String sql =
            """
            SELECT
                id,
                species_id,
                name,
                type1,
                type2,
                base_hp,
                base_attack,
                base_defense,
                base_sp_attack,
                base_sp_defense,
                base_speed,
                cry_id
            FROM pokemon_forms
            """;

        List<PokemonForm> pokemonForms = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonForms.add(
                    new PokemonForm(
                        result.getInt("id"),
                        result.getInt("species_id"),
                        result.getString("name"),
                        result.getString("type1"),
                        result.getString("type2"),
                        result.getInt("base_hp"),
                        result.getInt("base_attack"),
                        result.getInt("base_defense"),
                        result.getInt("base_sp_attack"),
                        result.getInt("base_sp_defense"),
                        result.getInt("base_speed"),
                        result.getInt("cry_id")
                    )
                );

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return pokemonForms;

    }

    public void updatePokemonForm(PokemonForm pokemonForm) {

        String sql =
            """
            UPDATE pokemon_forms
            SET
                species_id = ?,
                name = ?,
                type1 = ?,
                type2 = ?,
                base_hp = ?,
                base_attack = ?,
                base_defense = ?,
                base_sp_attack = ?,
                base_sp_defense = ?,
                base_speed = ?,
                cry_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonForm.getSpeciesId());
            statement.setString(2, pokemonForm.getName());
            statement.setString(3, pokemonForm.getType1());
            statement.setString(4, pokemonForm.getType2());
            statement.setInt(5, pokemonForm.getBaseHp());
            statement.setInt(6, pokemonForm.getBaseAttack());
            statement.setInt(7, pokemonForm.getBaseDefense());
            statement.setInt(8, pokemonForm.getBaseSpAttack());
            statement.setInt(9, pokemonForm.getBaseSpDefense());
            statement.setInt(10, pokemonForm.getBaseSpeed());
            statement.setInt(11, pokemonForm.getCryId());
            statement.setInt(12, pokemonForm.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public void deletePokemonForm(int id) {

        String sql =
            """
            DELETE FROM pokemon_forms
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }
    
    
  //=================POKEMON SPRITE METHODS===========

    public void createPokemonSprite(PokemonSprite pokemonSprite) {

        String sql =
            """
            INSERT INTO pokemon_sprites (
                id,
                form_id,
                normal_sprite,
                shiny_sprite
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonSprite.getId());
            statement.setInt(2, pokemonSprite.getFormId());
            statement.setString(3, pokemonSprite.getNormalSprite());
            statement.setString(4, pokemonSprite.getShinySprite());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public PokemonSprite findPokemonSpriteById(int id) {

        String sql =
            """
            SELECT
                id,
                form_id,
                normal_sprite,
                shiny_sprite
            FROM pokemon_sprites
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonSprite(
                        result.getInt("id"),
                        result.getInt("form_id"),
                        result.getString("normal_sprite"),
                        result.getString("shiny_sprite")
                    );

                }

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return null;

    }

    public List<PokemonSprite> findAllPokemonSprites() {

        String sql =
            """
            SELECT
                id,
                form_id,
                normal_sprite,
                shiny_sprite
            FROM pokemon_sprites
            """;

        List<PokemonSprite> pokemonSprites = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonSprites.add(
                    new PokemonSprite(
                        result.getInt("id"),
                        result.getInt("form_id"),
                        result.getString("normal_sprite"),
                        result.getString("shiny_sprite")
                    )
                );

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return pokemonSprites;

    }

    public void updatePokemonSprite(PokemonSprite pokemonSprite) {

        String sql =
            """
            UPDATE pokemon_sprites
            SET
                form_id = ?,
                normal_sprite = ?,
                shiny_sprite = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonSprite.getFormId());
            statement.setString(2, pokemonSprite.getNormalSprite());
            statement.setString(3, pokemonSprite.getShinySprite());
            statement.setInt(4, pokemonSprite.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public void deletePokemonSprite(int id) {

        String sql =
            """
            DELETE FROM pokemon_sprites
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }
    
  //=================EVOLUTION METHODS===========

    public void createEvolution(Evolution evolution) {

        String sql =
            """
            INSERT INTO evolutions (
                id,
                from_species_id,
                from_form_id,
                to_species_id,
                to_form_id
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, evolution.getId());
            statement.setInt(2, evolution.getFromSpeciesId());
            statement.setInt(3, evolution.getFromFormId());
            statement.setInt(4, evolution.getToSpeciesId());
            statement.setInt(5, evolution.getToFormId());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public Evolution findEvolutionById(int id) {

        String sql =
            """
            SELECT
                id,
                from_species_id,
                from_form_id,
                to_species_id,
                to_form_id
            FROM evolutions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Evolution(
                        result.getInt("id"),
                        result.getInt("from_species_id"),
                        result.getInt("from_form_id"),
                        result.getInt("to_species_id"),
                        result.getInt("to_form_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Evolution> findAllEvolutions() {

        String sql =
            """
            SELECT
                id,
                from_species_id,
                from_form_id,
                to_species_id,
                to_form_id
            FROM evolutions
            """;

        List<Evolution> evolutions = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                evolutions.add(
                    new Evolution(
                        result.getInt("id"),
                        result.getInt("from_species_id"),
                        result.getInt("from_form_id"),
                        result.getInt("to_species_id"),
                        result.getInt("to_form_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evolutions;
    }

    public void updateEvolution(Evolution evolution) {

        String sql =
            """
            UPDATE evolutions
            SET
                from_species_id = ?,
                from_form_id = ?,
                to_species_id = ?,
                to_form_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, evolution.getFromSpeciesId());
            statement.setInt(2, evolution.getFromFormId());
            statement.setInt(3, evolution.getToSpeciesId());
            statement.setInt(4, evolution.getToFormId());
            statement.setInt(5, evolution.getId());
            statement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEvolution(int id) {

        String sql =
            """
            DELETE FROM evolutions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //=================EVOLUTION CONDITION METHODS===========

    public void createEvolutionCondition(EvolutionCondition evolutionCondition) {

        String sql =
            """
            INSERT INTO evolution_conditions (
                id,
                evolution_id,
                condition_type,
                value
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, evolutionCondition.getId());
            statement.setInt(2, evolutionCondition.getEvolutionId());
            statement.setString(3, evolutionCondition.getConditionType());
            statement.setString(4, evolutionCondition.getValue());
            statement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public EvolutionCondition findEvolutionConditionById(int id) {

        String sql =
            """
            SELECT
                id,
                evolution_id,
                condition_type,
                value
            FROM evolution_conditions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new EvolutionCondition(
                        result.getInt("id"),
                        result.getInt("evolution_id"),
                        result.getString("condition_type"),
                        result.getString("value")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EvolutionCondition> findAllEvolutionConditions() {

        String sql =
            """
            SELECT
                id,
                evolution_id,
                condition_type,
                value
            FROM evolution_conditions
            """;

        List<EvolutionCondition> evolutionConditions =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                evolutionConditions.add(
                    new EvolutionCondition(
                        result.getInt("id"),
                        result.getInt("evolution_id"),
                        result.getString("condition_type"),
                        result.getString("value")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evolutionConditions;
    }

    public void updateEvolutionCondition(
        EvolutionCondition evolutionCondition
    ) {

        String sql =
            """
            UPDATE evolution_conditions
            SET
                evolution_id = ?,
                condition_type = ?,
                value = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, evolutionCondition.getEvolutionId());
            statement.setString(2, evolutionCondition.getConditionType());
            statement.setString(3, evolutionCondition.getValue());
            statement.setInt(4, evolutionCondition.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEvolutionCondition(int id) {

        String sql =
            """
            DELETE FROM evolution_conditions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
  //=================ABILITY METHODS===========

    public void createAbility(Ability ability) {

        String sql =
            """
            INSERT INTO abilities (
                id,
                name,
                description
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, ability.getId());
            statement.setString(2, ability.getName());
            statement.setString(3, ability.getDescription());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Ability findAbilityById(int id) {

        String sql =
            """
            SELECT
                id,
                name,
                description
            FROM abilities
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Ability(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Ability> findAllAbilities() {

        String sql =
            """
            SELECT
                id,
                name,
                description
            FROM abilities
            """;

        List<Ability> abilities = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                abilities.add(
                    new Ability(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description")
                    )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
        return abilities;
    }

    public void updateAbility(Ability ability) {

        String sql =
            """
            UPDATE abilities
            SET
                name = ?,
                description = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, ability.getName());
            statement.setString(2, ability.getDescription());
            statement.setInt(3, ability.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAbility(int id) {

        String sql =
            """
            DELETE FROM abilities
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //=================FORM ABILITY METHODS===========

    public void createFormAbility(FormAbility formAbility) {

        String sql =
            """
            INSERT INTO form_abilities (
                form_id,
                ability_id,
                is_hidden
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formAbility.getFormId());
            statement.setInt(2, formAbility.getAbilityId());
            statement.setBoolean(3, formAbility.isHidden());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public FormAbility findFormAbility(
        int formId,
        int abilityId
    ) {

        String sql =
            """
            SELECT
                form_id,
                ability_id,
                is_hidden
            FROM form_abilities
            WHERE form_id = ?
            AND ability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formId);
            statement.setInt(2, abilityId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new FormAbility(
                        result.getInt("form_id"),
                        result.getInt("ability_id"),
                        result.getBoolean("is_hidden")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FormAbility> findAllFormAbilities() {

        String sql =
            """
            SELECT
                form_id,
                ability_id,
                is_hidden
            FROM form_abilities
            """;

        List<FormAbility> formAbilities = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                formAbilities.add(
                    new FormAbility(
                        result.getInt("form_id"),
                        result.getInt("ability_id"),
                        result.getBoolean("is_hidden")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formAbilities;
    }

    public void updateFormAbility(FormAbility formAbility) {

        String sql =
            """
            UPDATE form_abilities
            SET
                is_hidden = ?
            WHERE form_id = ?
            AND ability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setBoolean(1, formAbility.isHidden());
            statement.setInt(2, formAbility.getFormId());
            statement.setInt(3, formAbility.getAbilityId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFormAbility(
        int formId,
        int abilityId
    ) {

        String sql =
            """
            DELETE FROM form_abilities
            WHERE form_id = ?
            AND ability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formId);
            statement.setInt(2, abilityId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //=================NATURE METHODS===========

    public void createNature(Nature nature) {

        String sql =
            """
            INSERT INTO natures (
                id,
                name,
                increased_attribute,
                decreased_attribute
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, nature.getId());
            statement.setString(2, nature.getName());
            statement.setString(3, nature.getIncreasedAttribute());
            statement.setString(4, nature.getDecreasedAttribute());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Nature findNatureById(int id) {

        String sql =
            """
            SELECT
                id,
                name,
                increased_attribute,
                decreased_attribute
            FROM natures
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Nature(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("increased_attribute"),
                        result.getString("decreased_attribute")
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
        return null;
    }

    public List<Nature> findAllNatures() {

        String sql =
            """
            SELECT
                id,
                name,
                increased_attribute,
                decreased_attribute
            FROM natures
            """;

        List<Nature> natures = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                natures.add(
                    new Nature(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("increased_attribute"),
                        result.getString("decreased_attribute")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return natures;
    }

    public void updateNature(Nature nature) {

        String sql =
            """
            UPDATE natures
            SET
                name = ?,
                increased_attribute = ?,
                decreased_attribute = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, nature.getName());
            statement.setString(2, nature.getIncreasedAttribute());
            statement.setString(3, nature.getDecreasedAttribute());
            statement.setInt(4, nature.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNature(int id) {

        String sql =
            """
            DELETE FROM natures
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  
  //=================MOVE METHODS===========

    public void createMove(Move move) {

        String sql =
            """
            INSERT INTO moves (
                id,
                name,
                type,
                frequency,
                accuracy,
                move_class,
                range,
                damage,
                description
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, move.getId());
            statement.setString(2, move.getName());
            statement.setString(3, move.getType());
            statement.setString(4, move.getFrequency());
            statement.setInt(5, move.getAccuracy());
            statement.setString(6, move.getMoveClass());
            statement.setString(7, move.getRange());
            statement.setString(8, move.getDamage());
            statement.setString(9, move.getDescription());
            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public Move findMoveById(int id) {

        String sql =
            """
            SELECT
                id,
                name,
                type,
                frequency,
                accuracy,
                move_class,
                range,
                damage,
                description
            FROM moves
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Move(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("type"),
                        result.getString("frequency"),
                        result.getInt("accuracy"),
                        result.getString("move_class"),
                        result.getString("range"),
                        result.getString("damage"),
                        result.getString("description")
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
        return null;
    }

    public List<Move> findAllMoves() {

        String sql =
            """
            SELECT
                id,
                name,
                type,
                frequency,
                accuracy,
                move_class,
                range,
                damage,
                description
            FROM moves
            """;

        List<Move> moves = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                moves.add(
                    new Move(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("type"),
                        result.getString("frequency"),
                        result.getInt("accuracy"),
                        result.getString("move_class"),
                        result.getString("range"),
                        result.getString("damage"),
                        result.getString("description")
                    )
                );
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
        return moves;
    }

    public void updateMove(Move move) {

        String sql =
            """
            UPDATE moves
            SET
                name = ?,
                type = ?,
                frequency = ?,
                accuracy = ?,
                move_class = ?,
                range = ?,
                damage = ?,
                description = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, move.getName());
            statement.setString(2, move.getType());
            statement.setString(3, move.getFrequency());
            statement.setInt(4, move.getAccuracy());
            statement.setString(5, move.getMoveClass());
            statement.setString(6, move.getRange());
            statement.setString(7, move.getDamage());
            statement.setString(8, move.getDescription());
            statement.setInt(9, move.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMove(int id) {

        String sql =
            """
            DELETE FROM moves
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //=================SKILL METHODS===========

    public void createSkill(Skill skill) {

        String sql =
            """
            INSERT INTO skills (
                id,
                name
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, skill.getId());
            statement.setString(2, skill.getName());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Skill findSkillById(int id) {

        String sql =
            """
            SELECT
                id,
                name
            FROM skills
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Skill(
                        result.getInt("id"),
                        result.getString("name")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Skill> findAllSkills() {

        String sql =
            """
            SELECT
                id,
                name
            FROM skills
            """;

        List<Skill> skills = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                skills.add(
                    new Skill(
                        result.getInt("id"),
                        result.getString("name")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return skills;
    }

    public void updateSkill(Skill skill) {

        String sql =
            """
            UPDATE skills
            SET
                name = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, skill.getName());
            statement.setInt(2, skill.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSkill(int id) {

        String sql =
            """
            DELETE FROM skills
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //=================FORM SKILL METHODS===========

    public void createFormSkill(FormSkill formSkill) {

        String sql =
            """
            INSERT INTO form_skills (
                form_id,
                skill_id
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formSkill.getFormId());
            statement.setInt(2, formSkill.getSkillId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public FormSkill findFormSkill(int formId, int skillId) {

        String sql =
            """
            SELECT
                form_id,
                skill_id
            FROM form_skills
            WHERE form_id = ?
            AND skill_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formId);
            statement.setInt(2, skillId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new FormSkill(
                        result.getInt("form_id"),
                        result.getInt("skill_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FormSkill> findAllFormSkills() {

        String sql =
            """
            SELECT
                form_id,
                skill_id
            FROM form_skills
            """;

        List<FormSkill> formSkills = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                formSkills.add(
                    new FormSkill(
                        result.getInt("form_id"),
                        result.getInt("skill_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formSkills;
    }

    public void updateFormSkill(
        FormSkill formSkill,
        int originalFormId,
        int originalSkillId
    ) {

        String sql =
            """
            UPDATE form_skills
            SET
                form_id = ?,
                skill_id = ?
            WHERE form_id = ?
            AND skill_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formSkill.getFormId());
            statement.setInt(2, formSkill.getSkillId());
            statement.setInt(3, originalFormId);
            statement.setInt(4, originalSkillId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFormSkill(int formId, int skillId) {

        String sql =
            """
            DELETE FROM form_skills
            WHERE form_id = ?
            AND skill_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formId);
            statement.setInt(2, skillId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //=================CAPABILITY METHODS===========

    public void createCapability(Capability capability) {

        String sql =
            """
            INSERT INTO capabilities (
                id,
                name
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, capability.getId());
            statement.setString(2, capability.getName());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Capability findCapabilityById(int id) {

        String sql =
            """
            SELECT
                id,
                name
            FROM capabilities
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Capability(
                        result.getInt("id"),
                        result.getString("name")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Capability> findAllCapabilities() {

        String sql =
            """
            SELECT
                id,
                name
            FROM capabilities
            """;

        List<Capability> capabilities = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                capabilities.add(
                    new Capability(
                        result.getInt("id"),
                        result.getString("name")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return capabilities;
    }

    public void updateCapability(Capability capability) {

        String sql =
            """
            UPDATE capabilities
            SET
                name = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, capability.getName());
            statement.setInt(2, capability.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteCapability(int id) {

        String sql =
            """
            DELETE FROM capabilities
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //=================FORM CAPABILITY METHODS===========

    public void createFormCapability(FormCapability formCapability) {

        String sql =
            """
            INSERT INTO form_capabilities (
                form_id,
                capability_id,
                value
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formCapability.getFormId());
            statement.setInt(2, formCapability.getCapabilityId());
            statement.setString(3, formCapability.getValue());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public FormCapability findFormCapability(
        int formId,
        int capabilityId
    ) {

        String sql =
            """
            SELECT
                form_id,
                capability_id,
                value
            FROM form_capabilities
            WHERE form_id = ?
            AND capability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formId);
            statement.setInt(2, capabilityId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new FormCapability(
                        result.getInt("form_id"),
                        result.getInt("capability_id"),
                        result.getString("value")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<FormCapability> findAllFormCapabilities() {

        String sql =
            """
            SELECT
                form_id,
                capability_id,
                value
            FROM form_capabilities
            """;

        List<FormCapability> formCapabilities =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                formCapabilities.add(
                    new FormCapability(
                        result.getInt("form_id"),
                        result.getInt("capability_id"),
                        result.getString("value")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return formCapabilities;
    }

    public void updateFormCapability(
        FormCapability formCapability,
        int originalFormId,
        int originalCapabilityId
    ) {

        String sql =
            """
            UPDATE form_capabilities
            SET
                form_id = ?,
                capability_id = ?,
                value = ?
            WHERE form_id = ?
            AND capability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formCapability.getFormId());
            statement.setInt(2, formCapability.getCapabilityId());
            statement.setString(3, formCapability.getValue());
            statement.setInt(4, originalFormId);
            statement.setInt(5, originalCapabilityId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteFormCapability(
        int formId,
        int capabilityId
    ) {

        String sql =
            """
            DELETE FROM form_capabilities
            WHERE form_id = ?
            AND capability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, formId);
            statement.setInt(2, capabilityId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    public void createItem(Item item) {

        String sql =
            """
            INSERT INTO items (
                id,
                name,
                description,
                category,
                effect
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, item.getId());
            statement.setString(2, item.getName());
            statement.setString(3, item.getDescription());
            statement.setString(4, item.getCategory());
            statement.setString(5, item.getEffect());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Item findItemById(int id) {

        String sql =
            """
            SELECT
                id,
                name,
                description,
                category,
                effect
            FROM items
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Item(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("category"),
                        result.getString("effect")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Item> findAllItems() {

        String sql =
            """
            SELECT
                id,
                name,
                description,
                category,
                effect
            FROM items
            """;

        List<Item> items =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                items.add(
                    new Item(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("description"),
                        result.getString("category"),
                        result.getString("effect")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public void updateItem(Item item) {

        String sql =
            """
            UPDATE items
            SET
                name = ?,
                description = ?,
                category = ?,
                effect = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, item.getName());
            statement.setString(2, item.getDescription());
            statement.setString(3, item.getCategory());
            statement.setString(4, item.getEffect());
            statement.setInt(5, item.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(int id) {

        String sql =
            """
            DELETE FROM items
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {
        	
            statement.setInt(1, id);
            statement.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //=================TRAINER METHODS===========
    public void createTrainer(Trainer trainer) {

        String sql =
            """
            INSERT INTO trainers (
                id,
                name,
                level,
                hp,
                money,
                player_id
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, trainer.getId());
            statement.setString(2, trainer.getName());
            statement.setInt(3, trainer.getLevel());
            statement.setInt(4, trainer.getHp());
            statement.setInt(5, trainer.getMoney());
            statement.setInt(6, trainer.getPlayerId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Trainer findTrainerById(int id) {

        String sql =
            """
            SELECT
                id,
                name,
                level,
                hp,
                money,
                player_id
            FROM trainers
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Trainer(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getInt("level"),
                        result.getInt("hp"),
                        result.getInt("money"),
                        result.getInt("player_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Trainer> findAllTrainers() {

        String sql =
            """
            SELECT
                id,
                name,
                level,
                hp,
                money,
                player_id
            FROM trainers
            """;

        List<Trainer> trainers =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                trainers.add(
                    new Trainer(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getInt("level"),
                        result.getInt("hp"),
                        result.getInt("money"),
                        result.getInt("player_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trainers;
    }

    public void updateTrainer(Trainer trainer) {

        String sql =
            """
            UPDATE trainers
            SET
                name = ?,
                level = ?,
                hp = ?,
                money = ?,
                player_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(1, trainer.getName());
            statement.setInt(2, trainer.getLevel());
            statement.setInt(3, trainer.getHp());
            statement.setInt(4, trainer.getMoney());
            statement.setInt(5, trainer.getPlayerId());
            statement.setInt(6, trainer.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTrainer(int id) {

        deleteChildren("trainer_inventory", "trainer_id", id);
        deleteChildren("trainer_attribute_points", "trainer_id", id);
        runDelete("DELETE FROM pc_boxes WHERE pc_id IN (SELECT id FROM pcs WHERE trainer_id = ?)", id);
        deleteChildren("pcs", "trainer_id", id);
        String sql =
            """
            DELETE FROM trainers
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
  //============CHARACTER SHEETS=============

    public void createSheet(CharacterSheet sheet) {
        try (PreparedStatement st = getConnection().prepareStatement(
                "INSERT INTO character_sheets (id, owner, name) VALUES (?, ?, ?)")) {
            st.setInt(1, sheet.getId());
            st.setString(2, sheet.getOwner());
            st.setString(3, sheet.getName());
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CharacterSheet> findAllSheets() {
        List<CharacterSheet> sheets = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement("SELECT id, owner, name FROM character_sheets ORDER BY id");
             ResultSet r = st.executeQuery()) {
            while (r.next()) sheets.add(new CharacterSheet(r.getInt("id"), r.getString("owner"), r.getString("name")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sheets;
    }

    public void deleteSheet(int id) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM character_sheets WHERE id = ?")) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlacement(int trainerId, int sheetId) {
        try (PreparedStatement st = getConnection().prepareStatement("INSERT INTO sheet_placements (trainer_id, sheet_id) VALUES (?, ?)")) {
            st.setInt(1, trainerId);
            st.setInt(2, sheetId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // [trainerId, sheetId] pairs
    public List<int[]> findAllPlacements() {
        List<int[]> list = new ArrayList<>();
        try (PreparedStatement st = getConnection().prepareStatement("SELECT trainer_id, sheet_id FROM sheet_placements");
             ResultSet r = st.executeQuery()) {
            while (r.next()) list.add(new int[] { r.getInt("trainer_id"), r.getInt("sheet_id") });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deletePlacementOfTrainer(int trainerId) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM sheet_placements WHERE trainer_id = ?")) {
            st.setInt(1, trainerId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlacementsOfSheet(int sheetId) {
        try (PreparedStatement st = getConnection().prepareStatement("DELETE FROM sheet_placements WHERE sheet_id = ?")) {
            st.setInt(1, sheetId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

  //============MASTER=============

    public void createMaster(Master master) {

        String sql =
            """
            INSERT INTO masters (
                id,
                player_id
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, master.getId());
            statement.setInt(2, master.getPlayerId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Master findMasterById(int id) {

        String sql =
            """
            SELECT
                id,
                player_id
            FROM masters
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Master(
                        result.getInt("id"),
                        result.getInt("player_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Master> findAllMasters() {

        String sql =
            """
            SELECT
                id,
                player_id
            FROM masters
            """;

        List<Master> masters =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                masters.add(
                    new Master(
                        result.getInt("id"),
                        result.getInt("player_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return masters;
    }

    public void updateMaster(Master master) {

        String sql =
            """
            UPDATE masters
            SET
                player_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, master.getPlayerId());
            statement.setInt(2, master.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMaster(int id) {

        String sql =
            """
            DELETE FROM masters
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
    
    
  //============PC=============

    public void createPC(PC pc) {

        String sql =
            """
            INSERT INTO pcs (
                id,
                trainer_id
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pc.getId());
            statement.setInt(2, pc.getTrainerId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PC findPCById(int id) {

        String sql =
            """
            SELECT
                id,
                trainer_id
            FROM pcs
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PC(
                        result.getInt("id"),
                        result.getInt("trainer_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PC> findAllPCs() {

        String sql =
            """
            SELECT
                id,
                trainer_id
            FROM pcs
            """;

        List<PC> pcs =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pcs.add(
                    new PC(
                        result.getInt("id"),
                        result.getInt("trainer_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pcs;
    }

    public void updatePC(PC pc) {

        String sql =
            """
            UPDATE pcs
            SET
                trainer_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pc.getTrainerId());
            statement.setInt(2, pc.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePC(int id) {

        String sql =
            """
            DELETE FROM pcs
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
  //============PCBOX=============

    public void createPCBox(PCBox pcBox) {

        String sql =
            """
            INSERT INTO pc_boxes (
                id,
                pc_id,
                name,
                box_number,
                slot_limit
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pcBox.getId());
            statement.setInt(2, pcBox.getPcId());
            statement.setString(3, pcBox.getName());
            statement.setInt(4, pcBox.getBoxNumber());
            statement.setInt(5, pcBox.getSlotLimit());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PCBox findPCBoxById(int id) {

        String sql =
            """
            SELECT
                id,
                pc_id,
                name,
                box_number,
                slot_limit
            FROM pc_boxes
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PCBox(
                        result.getInt("id"),
                        result.getInt("pc_id"),
                        result.getString("name"),
                        result.getInt("box_number"),
                        result.getInt("slot_limit")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PCBox> findAllPCBoxes() {

        String sql =
            """
            SELECT
                id,
                pc_id,
                name,
                box_number,
                slot_limit
            FROM pc_boxes
            """;

        List<PCBox> pcBoxes =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pcBoxes.add(
                    new PCBox(
                        result.getInt("id"),
                        result.getInt("pc_id"),
                        result.getString("name"),
                        result.getInt("box_number"),
                        result.getInt("slot_limit")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pcBoxes;
    }

    public void updatePCBox(PCBox pcBox) {

        String sql =
            """
            UPDATE pc_boxes
            SET
                pc_id = ?,
                name = ?,
                box_number = ?,
                slot_limit = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pcBox.getPcId());
            statement.setString(2, pcBox.getName());
            statement.setInt(3, pcBox.getBoxNumber());
            statement.setInt(4, pcBox.getSlotLimit());
            statement.setInt(5, pcBox.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
           e.printStackTrace();
        }
    }

    public void deletePCBox(int id) {

        String sql =
            """
            DELETE FROM pc_boxes
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
  //============TRAINER INVENTORY=============

    public void createTrainerInventory(
        TrainerInventory trainerInventory
    ) {

        String sql =
            """
            INSERT INTO trainer_inventory (
                trainer_id,
                item_id,
                quantity
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                trainerInventory.getTrainerId()
            );

            statement.setInt(
                2,
                trainerInventory.getItemId()
            );

            statement.setInt(
                3,
                trainerInventory.getQuantity()
            );
            
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TrainerInventory findTrainerInventory(
        int trainerId,
        int itemId
    ) {

        String sql =
            """
            SELECT
                trainer_id,
                item_id,
                quantity
            FROM trainer_inventory
            WHERE trainer_id = ?
            AND item_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, trainerId);
            statement.setInt(2, itemId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new TrainerInventory(
                        result.getInt("trainer_id"),
                        result.getInt("item_id"),
                        result.getInt("quantity")
                   );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TrainerInventory> findAllTrainerInventories() {

        String sql =
            """
            SELECT
                trainer_id,
                item_id,
                quantity
            FROM trainer_inventory
            """;

        List<TrainerInventory> inventories =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                inventories.add(
                    new TrainerInventory(
                        result.getInt("trainer_id"),
                        result.getInt("item_id"),
                        result.getInt("quantity")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventories;
    }

    public void updateTrainerInventory(
        TrainerInventory trainerInventory,
        int originalTrainerId,
        int originalItemId
    ) {

        String sql =
            """
            UPDATE trainer_inventory
            SET
                trainer_id = ?,
                item_id = ?,
                quantity = ?
            WHERE trainer_id = ?
            AND item_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                trainerInventory.getTrainerId()
            );

            statement.setInt(
                2,
                trainerInventory.getItemId()
            );

            statement.setInt(
                3,
                trainerInventory.getQuantity()
            );

            statement.setInt(4, originalTrainerId);
            statement.setInt(5, originalItemId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTrainerInventory(
        int trainerId,
        int itemId
    ) {

        String sql =
            """
            DELETE FROM trainer_inventory
            WHERE trainer_id = ?
            AND item_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, trainerId);
            statement.setInt(2, itemId);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
  //============POKEMON=============

    public void createPokemon(Pokemon pokemon) {

        String sql =
            """
            INSERT INTO pokemon (
                id,
                trainer_id,
                species_id,
                form_id,
                nickname,
                gender,
                shiny,
                nature_id,
                ability_id,
                level,
                xp,
                hp,
                undistributed_attribute_points,
                held_item_id,
                party_slot,
                pc_box_id,
                pc_slot,
                captured_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            if (pokemon.getTrainerId() == null) {

                statement.setNull(2, java.sql.Types.INTEGER);

            } else {

                statement.setInt(2, pokemon.getTrainerId());

            }

            statement.setInt(1, pokemon.getId());
            statement.setInt(3, pokemon.getSpeciesId());
            statement.setInt(4, pokemon.getFormId());
            statement.setString(5, pokemon.getNickname());
            statement.setString(6, pokemon.getGender());
            statement.setBoolean(7, pokemon.isShiny());
            statement.setInt(8, pokemon.getNatureId());
            statement.setInt(9, pokemon.getAbilityId());
            statement.setInt(10, pokemon.getLevel());
            statement.setInt(11, pokemon.getXp());
            statement.setFloat(12, pokemon.getHp());
            statement.setInt(
                13,
                pokemon.getUndistributedAttributePoints()
            );
            statement.setInt(14, pokemon.getHeldItemId());
            statement.setInt(15, pokemon.getPartySlot());
            statement.setInt(16, pokemon.getPcBoxId());
            statement.setInt(17, pokemon.getPcSlot());
            statement.setTimestamp(
                18,
                java.sql.Timestamp.valueOf(
                    pokemon.getCapturedAt()
                )
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Pokemon findPokemonById(int id) {

        String sql =
            """
            SELECT
                id,
                trainer_id,
                species_id,
                form_id,
                nickname,
                gender,
                shiny,
                nature_id,
                ability_id,
                level,
                xp,
                hp,
                undistributed_attribute_points,
                held_item_id,
                party_slot,
                pc_box_id,
                pc_slot,
                captured_at
            FROM pokemon
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    Integer trainerId =
                        (Integer) result.getObject("trainer_id");

                    java.sql.Timestamp capturedAt =
                        result.getTimestamp("captured_at");

                    return new Pokemon(
                        result.getInt("id"),
                        trainerId,
                        result.getInt("species_id"),
                        result.getInt("form_id"),
                        result.getString("nickname"),
                        result.getString("gender"),
                        result.getBoolean("shiny"),
                        result.getInt("nature_id"),
                        result.getInt("ability_id"),
                        result.getInt("level"),
                        result.getInt("xp"),
                        result.getFloat("hp"),
                        result.getInt(
                            "undistributed_attribute_points"
                        ),
                        result.getInt("held_item_id"),
                        result.getInt("party_slot"),
                        result.getInt("pc_box_id"),
                        result.getInt("pc_slot"),
                        capturedAt.toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Pokemon> findAllPokemons() {

        String sql =
            """
            SELECT
                id,
                trainer_id,
                species_id,
                form_id,
                nickname,
                gender,
                shiny,
                nature_id,
                ability_id,
                level,
                xp,
                hp,
                undistributed_attribute_points,
                held_item_id,
                party_slot,
                pc_box_id,
                pc_slot,
                captured_at
            FROM pokemon
            """;

        List<Pokemon> pokemons =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                Integer trainerId =
                    (Integer) result.getObject("trainer_id");

                java.sql.Timestamp capturedAt =
                    result.getTimestamp("captured_at");

                pokemons.add(
                    new Pokemon(
                        result.getInt("id"),
                        trainerId,
                        result.getInt("species_id"),
                        result.getInt("form_id"),
                        result.getString("nickname"),
                        result.getString("gender"),
                        result.getBoolean("shiny"),
                        result.getInt("nature_id"),
                        result.getInt("ability_id"),
                        result.getInt("level"),
                        result.getInt("xp"),
                        result.getFloat("hp"),
                        result.getInt(
                            "undistributed_attribute_points"
                        ),
                        result.getInt("held_item_id"),
                        result.getInt("party_slot"),
                        result.getInt("pc_box_id"),
                        result.getInt("pc_slot"),
                        capturedAt.toLocalDateTime()
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pokemons;
    }

    public void updatePokemon(Pokemon pokemon) {

        String sql =
            """
            UPDATE pokemon
            SET
                trainer_id = ?,
                species_id = ?,
                form_id = ?,
                nickname = ?,
                gender = ?,
                shiny = ?,
                nature_id = ?,
                ability_id = ?,
                level = ?,
                xp = ?,
                hp = ?,
                undistributed_attribute_points = ?,
                held_item_id = ?,
                party_slot = ?,
                pc_box_id = ?,
                pc_slot = ?,
                captured_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            if (pokemon.getTrainerId() == null) {

                statement.setNull(1, java.sql.Types.INTEGER);

            } else {

                statement.setInt(1, pokemon.getTrainerId());

            }

            statement.setInt(2, pokemon.getSpeciesId());
            statement.setInt(3, pokemon.getFormId());
            statement.setString(4, pokemon.getNickname());
            statement.setString(5, pokemon.getGender());
            statement.setBoolean(6, pokemon.isShiny());
            statement.setInt(7, pokemon.getNatureId());
            statement.setInt(8, pokemon.getAbilityId());
            statement.setInt(9, pokemon.getLevel());
            statement.setInt(10, pokemon.getXp());
            statement.setFloat(11, pokemon.getHp());
            statement.setInt(
                12,
                pokemon.getUndistributedAttributePoints()
            );
            statement.setInt(13, pokemon.getHeldItemId());
            statement.setInt(14, pokemon.getPartySlot());
            statement.setInt(15, pokemon.getPcBoxId());
            statement.setInt(16, pokemon.getPcSlot());
            statement.setTimestamp(
                17,
                java.sql.Timestamp.valueOf(
                    pokemon.getCapturedAt()
                )
            );
            statement.setInt(18, pokemon.getId());

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    //============CASCADE HELPER=============

    // SQLite FKs have no ON DELETE CASCADE here, so children go first.
    // Table/column names are fixed literals (never user input).
    // Stats of this Pokemon that ignore the Base Relation rule (a Master-granted exception)
    public List<String> findStatExceptions(int pokemonId) {

        List<String> attributes = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement("SELECT attribute FROM pokemon_stat_exceptions WHERE pokemon_id = ?")) {

            statement.setInt(1, pokemonId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) attributes.add(result.getString("attribute"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attributes;
    }

    public void setStatException(int pokemonId, String attribute, boolean on) {

        String sql = on
            ? "INSERT OR IGNORE INTO pokemon_stat_exceptions (pokemon_id, attribute) VALUES (?, ?)"
            : "DELETE FROM pokemon_stat_exceptions WHERE pokemon_id = ? AND attribute = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setString(2, attribute);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteChildren(String table, String column, int id) {

        runDelete("DELETE FROM " + table + " WHERE " + column + " = ?", id);

    }

    private void runDelete(String sql, int id) {

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public void deletePokemon(int id) {

        for (String table : new String[] {
                "pokemon_attribute_points", "pokemon_vitamins", "pokemon_capabilities",
                "pokemon_moves", "pokemon_skills", "pokemon_conditions", "pokemon_stat_exceptions", "trade_offers" }) {
            deleteChildren(table, "pokemon_id", id);
        }

        String sql =
            """
            DELETE FROM pokemon
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }
    
  //============POKEMON ATTRIBUTE POINTS=============

    public void createPokemonAttributePoints(
        PokemonAttributePoints pokemonAttributePoints
    ) {

        String sql =
            """
            INSERT INTO pokemon_attribute_points (
                pokemon_id,
                attribute,
                points
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonAttributePoints.getPokemonId()
            );

            statement.setString(
                2,
                pokemonAttributePoints.getAttribute()
            );

            statement.setInt(
                3,
                pokemonAttributePoints.getPoints()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PokemonAttributePoints findPokemonAttributePoints(
        int pokemonId,
        String attribute
    ) {

        String sql =
            """
            SELECT
                pokemon_id,
                attribute,
                points
            FROM pokemon_attribute_points
            WHERE pokemon_id = ?
            AND attribute = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setString(2, attribute);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonAttributePoints(
                        result.getInt("pokemon_id"),
                        result.getString("attribute"),
                        result.getInt("points")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PokemonAttributePoints>
    findAllPokemonAttributePoints() {

        String sql =
            """
            SELECT
                pokemon_id,
                attribute,
                points
            FROM pokemon_attribute_points
            """;

        List<PokemonAttributePoints> attributePoints =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                attributePoints.add(
                    new PokemonAttributePoints(
                        result.getInt("pokemon_id"),
                        result.getString("attribute"),
                        result.getInt("points")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attributePoints;
    }

    public void updatePokemonAttributePoints(
        PokemonAttributePoints pokemonAttributePoints,
        int originalPokemonId,
        String originalAttribute
    ) {

        String sql =
            """
            UPDATE pokemon_attribute_points
            SET
                pokemon_id = ?,
                attribute = ?,
                points = ?
            WHERE pokemon_id = ?
            AND attribute = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonAttributePoints.getPokemonId()
            );

            statement.setString(
                2,
                pokemonAttributePoints.getAttribute()
            );

            statement.setInt(
                3,
                pokemonAttributePoints.getPoints()
            );

            statement.setInt(4, originalPokemonId);
            statement.setString(5, originalAttribute);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePokemonAttributePoints(
        int pokemonId,
        String attribute
    ) {

        String sql =
            """
            DELETE FROM pokemon_attribute_points
            WHERE pokemon_id = ?
            AND attribute = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setString(2, attribute);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //============POKEMON VITAMIN=============

    public void createPokemonVitamin(
        PokemonVitamin pokemonVitamin
    ) {

        String sql =
            """
            INSERT INTO pokemon_vitamins (
                pokemon_id,
                vitamin_id,
                quantity
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonVitamin.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonVitamin.getVitaminId()
            );

            statement.setInt(
                3,
                pokemonVitamin.getQuantity()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PokemonVitamin findPokemonVitamin(
        int pokemonId,
        int vitaminId
    ) {

        String sql =
            """
            SELECT
                pokemon_id,
                vitamin_id,
                quantity
            FROM pokemon_vitamins
            WHERE pokemon_id = ?
            AND vitamin_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, vitaminId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonVitamin(
                        result.getInt("pokemon_id"),
                        result.getInt("vitamin_id"),
                        result.getInt("quantity")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PokemonVitamin> findAllPokemonVitamins() {

        String sql =
            """
            SELECT
                pokemon_id,
                vitamin_id,
                quantity
            FROM pokemon_vitamins
            """;

        List<PokemonVitamin> pokemonVitamins =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonVitamins.add(
                    new PokemonVitamin(
                        result.getInt("pokemon_id"),
                        result.getInt("vitamin_id"),
                        result.getInt("quantity")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pokemonVitamins;
    }

    public void updatePokemonVitamin(
        PokemonVitamin pokemonVitamin,
        int originalPokemonId,
        int originalVitaminId
    ) {

        String sql =
            """
            UPDATE pokemon_vitamins
            SET
                pokemon_id = ?,
                vitamin_id = ?,
                quantity = ?
            WHERE pokemon_id = ?
            AND vitamin_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonVitamin.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonVitamin.getVitaminId()
            );

            statement.setInt(
                3,
                pokemonVitamin.getQuantity()
            );

            statement.setInt(4, originalPokemonId);
            statement.setInt(5, originalVitaminId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePokemonVitamin(
        int pokemonId,
        int vitaminId
    ) {

        String sql =
            """
            DELETE FROM pokemon_vitamins
            WHERE pokemon_id = ?
            AND vitamin_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, vitaminId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
  //============POKEMON CAPABILITY=============

    public void createPokemonCapability(
        PokemonCapability pokemonCapability
    ) {

        String sql =
            """
            INSERT INTO pokemon_capabilities (
                pokemon_id,
                capability_id,
                modifier
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonCapability.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonCapability.getCapabilityId()
            );

            statement.setInt(
                3,
                pokemonCapability.getModifier()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PokemonCapability findPokemonCapability(
        int pokemonId,
        int capabilityId
    ) {

        String sql =
            """
            SELECT
                pokemon_id,
                capability_id,
                modifier
            FROM pokemon_capabilities
            WHERE pokemon_id = ?
            AND capability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, capabilityId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonCapability(
                        result.getInt("pokemon_id"),
                        result.getInt("capability_id"),
                        result.getInt("modifier")
                    );
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PokemonCapability> findAllPokemonCapabilities() {

        String sql =
            """
            SELECT
                pokemon_id,
                capability_id,
                modifier
            FROM pokemon_capabilities
            """;

        List<PokemonCapability> pokemonCapabilities =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonCapabilities.add(
                    new PokemonCapability(
                        result.getInt("pokemon_id"),
                        result.getInt("capability_id"),
                        result.getInt("modifier")
                    )
                );
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pokemonCapabilities;
    }

    public void updatePokemonCapability(
        PokemonCapability pokemonCapability,
        int originalPokemonId,
        int originalCapabilityId
    ) {

        String sql =
            """
            UPDATE pokemon_capabilities
            SET
                pokemon_id = ?,
                capability_id = ?,
                modifier = ?
            WHERE pokemon_id = ?
            AND capability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonCapability.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonCapability.getCapabilityId()
            );

            statement.setInt(
                3,
                pokemonCapability.getModifier()
            );

            statement.setInt(4, originalPokemonId);
            statement.setInt(5, originalCapabilityId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePokemonCapability(
        int pokemonId,
        int capabilityId
    ) {

        String sql =
            """
            DELETE FROM pokemon_capabilities
            WHERE pokemon_id = ?
            AND capability_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, capabilityId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //============POKEMON MOVE=============

    public void createPokemonMove(
        PokemonMove pokemonMove
    ) {

        String sql =
            """
            INSERT INTO pokemon_moves (
                pokemon_id,
                move_id
            )
            VALUES (?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonMove.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonMove.getMoveId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PokemonMove findPokemonMove(
        int pokemonId,
        int moveId
    ) {

        String sql =
            """
            SELECT
                pokemon_id,
                move_id
            FROM pokemon_moves
            WHERE pokemon_id = ?
            AND move_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, moveId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonMove(
                        result.getInt("pokemon_id"),
                        result.getInt("move_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PokemonMove> findAllPokemonMoves() {

        String sql =
            """
            SELECT
                pokemon_id,
                move_id
            FROM pokemon_moves
            """;

        List<PokemonMove> pokemonMoves =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonMoves.add(
                    new PokemonMove(
                        result.getInt("pokemon_id"),
                        result.getInt("move_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pokemonMoves;
    }

    public void updatePokemonMove(
        PokemonMove pokemonMove,
        int originalPokemonId,
        int originalMoveId
    ) {

        String sql =
            """
            UPDATE pokemon_moves
            SET
                pokemon_id = ?,
                move_id = ?
            WHERE pokemon_id = ?
            AND move_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonMove.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonMove.getMoveId()
            );

            statement.setInt(3, originalPokemonId);
            statement.setInt(4, originalMoveId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePokemonMove(
        int pokemonId,
        int moveId
    ) {

        String sql =
            """
            DELETE FROM pokemon_moves
            WHERE pokemon_id = ?
            AND move_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, moveId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============POKEMON SKILL=============

    public void createPokemonSkill(
        PokemonSkill pokemonSkill
    ) {

        String sql =
            """
            INSERT INTO pokemon_skills (
                pokemon_id,
                skill_id,
                modifier
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonSkill.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonSkill.getSkillId()
            );

            statement.setInt(
                3,
                pokemonSkill.getModifier()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PokemonSkill findPokemonSkill(
        int pokemonId,
        int skillId
    ) {

        String sql =
            """
            SELECT
                pokemon_id,
                skill_id,
                modifier
            FROM pokemon_skills
            WHERE pokemon_id = ?
            AND skill_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, skillId);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonSkill(
                        result.getInt("pokemon_id"),
                        result.getInt("skill_id"),
                        result.getInt("modifier")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PokemonSkill> findAllPokemonSkills() {

        String sql =
            """
            SELECT
                pokemon_id,
                skill_id,
                modifier
            FROM pokemon_skills
            """;

        List<PokemonSkill> pokemonSkills =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonSkills.add(
                    new PokemonSkill(
                        result.getInt("pokemon_id"),
                        result.getInt("skill_id"),
                        result.getInt("modifier")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pokemonSkills;
    }

    public void updatePokemonSkill(
        PokemonSkill pokemonSkill,
        int originalPokemonId,
        int originalSkillId
    ) {

        String sql =
            """
            UPDATE pokemon_skills
            SET
                pokemon_id = ?,
                skill_id = ?,
                modifier = ?
            WHERE pokemon_id = ?
            AND skill_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonSkill.getPokemonId()
            );

            statement.setInt(
                2,
                pokemonSkill.getSkillId()
            );

            statement.setInt(
                3,
                pokemonSkill.getModifier()
            );

            statement.setInt(4, originalPokemonId);
            statement.setInt(5, originalSkillId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePokemonSkill(
        int pokemonId,
        int skillId
    ) {

        String sql =
            """
            DELETE FROM pokemon_skills
            WHERE pokemon_id = ?
            AND skill_id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, pokemonId);
            statement.setInt(2, skillId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============POKEMON CONDITION=============

    public void createPokemonCondition(
        PokemonCondition pokemonCondition
    ) {

        String sql =
            """
            INSERT INTO pokemon_conditions (
                id,
                pokemon_id,
                condition
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonCondition.getId()
            );

            statement.setInt(
                2,
                pokemonCondition.getPokemonId()
            );

            statement.setString(
                3,
                pokemonCondition.getCondition()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public PokemonCondition findPokemonConditionById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                pokemon_id,
                condition
            FROM pokemon_conditions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new PokemonCondition(
                        result.getInt("id"),
                        result.getInt("pokemon_id"),
                        result.getString("condition")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PokemonCondition> findAllPokemonConditions() {

        String sql =
            """
            SELECT
                id,
                pokemon_id,
                condition
            FROM pokemon_conditions
            """;

        List<PokemonCondition> pokemonConditions =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                pokemonConditions.add(
                    new PokemonCondition(
                        result.getInt("id"),
                        result.getInt("pokemon_id"),
                        result.getString("condition")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pokemonConditions;
    }

    public void updatePokemonCondition(
        PokemonCondition pokemonCondition
    ) {

        String sql =
            """
            UPDATE pokemon_conditions
            SET
                pokemon_id = ?,
                condition = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                pokemonCondition.getPokemonId()
            );

            statement.setString(
                2,
                pokemonCondition.getCondition()
            );

            statement.setInt(
                3,
                pokemonCondition.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePokemonCondition(
        int id
    ) {

        String sql =
            """
            DELETE FROM pokemon_conditions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============INJURY=============

    public void createInjury(
        Injury injury
    ) {

        String sql =
            """
            INSERT INTO injuries (
                id,
                created_at,
                owner_type,
                owner_id,
                removed_at
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                injury.getId()
            );

            statement.setTimestamp(
                2,
                java.sql.Timestamp.valueOf(
                    injury.getCreatedAt()
                )
            );

            statement.setString(
                3,
                injury.getOwnerType()
            );

            statement.setInt(
                4,
                injury.getOwnerId()
            );

            if (injury.getRemovedAt() == null) {

                statement.setNull(
                    5,
                    java.sql.Types.TIMESTAMP
                );

            } else {

                statement.setTimestamp(
                    5,
                    java.sql.Timestamp.valueOf(
                        injury.getRemovedAt()
                    )
                );
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Injury findInjuryById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                created_at,
                owner_type,
                owner_id,
                removed_at
            FROM injuries
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    java.sql.Timestamp removedAt =
                        result.getTimestamp("removed_at");

                    return new Injury(
                        result.getInt("id"),
                        result.getTimestamp("created_at")
                            .toLocalDateTime(),
                        result.getString("owner_type"),
                        result.getInt("owner_id"),
                        removedAt == null
                            ? null
                            : removedAt.toLocalDateTime()
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Injury> findAllInjuries() {

        String sql =
            """
            SELECT
                id,
                created_at,
                owner_type,
                owner_id,
                removed_at
            FROM injuries
            """;

        List<Injury> injuries =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                java.sql.Timestamp removedAt =
                    result.getTimestamp("removed_at");

                injuries.add(
                    new Injury(
                        result.getInt("id"),
                        result.getTimestamp("created_at")
                            .toLocalDateTime(),
                        result.getString("owner_type"),
                        result.getInt("owner_id"),
                        removedAt == null
                            ? null
                            : removedAt.toLocalDateTime()
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return injuries;
    }

    public void updateInjury(
        Injury injury
    ) {

        String sql =
            """
            UPDATE injuries
            SET
                created_at = ?,
                owner_type = ?,
                owner_id = ?,
                removed_at = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setTimestamp(
                1,
                java.sql.Timestamp.valueOf(
                    injury.getCreatedAt()
                )
            );

            statement.setString(
                2,
                injury.getOwnerType()
            );

            statement.setInt(
                3,
                injury.getOwnerId()
            );

            if (injury.getRemovedAt() == null) {

                statement.setNull(
                    4,
                    java.sql.Types.TIMESTAMP
                );

            } else {

                statement.setTimestamp(
                    4,
                    java.sql.Timestamp.valueOf(
                        injury.getRemovedAt()
                    )
                );
            }

            statement.setInt(
                5,
                injury.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteInjury(
        int id
    ) {

        String sql =
            """
            DELETE FROM injuries
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============GAME SESSION=============

    public void createGameSession(
        GameSession gameSession
    ) {

        String sql =
            """
            INSERT INTO game_sessions (
                id,
                current_day,
                host_player_id,
                join_code
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                gameSession.getId()
            );

            statement.setInt(
                2,
                gameSession.getCurrentDay()
            );

            statement.setInt(
                3,
                gameSession.getHostPlayerId()
            );

            statement.setString(
                4,
                gameSession.getJoinCode()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GameSession findGameSessionById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                current_day,
                host_player_id,
                join_code
            FROM game_sessions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new GameSession(
                        result.getInt("id"),
                        result.getInt("host_player_id"),
                        result.getInt("current_day"),
                        result.getString("join_code")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<GameSession> findAllGameSessions() {

        String sql =
            """
            SELECT
                id,
                current_day,
                host_player_id,
                join_code
            FROM game_sessions
            """;

        List<GameSession> gameSessions =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                gameSessions.add(
                    new GameSession(
                        result.getInt("id"),
                        result.getInt("host_player_id"),
                        result.getInt("current_day"),
                        result.getString("join_code")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gameSessions;
    }

    public void updateGameSession(
        GameSession gameSession
    ) {

        String sql =
            """
            UPDATE game_sessions
            SET
                current_day = ?,
                host_player_id = ?,
                join_code = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                gameSession.getCurrentDay()
            );

            statement.setInt(
                2,
                gameSession.getHostPlayerId()
            );

            statement.setString(
                3,
                gameSession.getJoinCode()
            );

            statement.setInt(
                4,
                gameSession.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteGameSession(
        int id
    ) {

        String sql =
            """
            DELETE FROM game_sessions
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============SESSION PARTICIPANT=============

    public void createSessionParticipant(
        SessionParticipant sessionParticipant
    ) {

        String sql =
            """
            INSERT INTO session_participants (
                id,
                game_session_id,
                player_id,
                type,
                participant_id,
                token
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                sessionParticipant.getId()
            );

            statement.setInt(
                2,
                sessionParticipant.getGameSessionId()
            );

            statement.setInt(
                3,
                sessionParticipant.getPlayerId()
            );

            statement.setString(
                4,
                sessionParticipant.getType()
            );

            statement.setInt(
                5,
                sessionParticipant.getParticipantId()
            );

            statement.setString(
                6,
                sessionParticipant.getToken()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SessionParticipant findSessionParticipantById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                game_session_id,
                player_id,
                type,
                participant_id,
                token
            FROM session_participants
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new SessionParticipant(
                        result.getInt("id"),
                        result.getInt("game_session_id"),
                        result.getInt("player_id"),
                        result.getString("type"),
                        result.getInt("participant_id"),
                        result.getString("token")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<SessionParticipant> findAllSessionParticipants() {

        String sql =
            """
            SELECT
                id,
                game_session_id,
                player_id,
                type,
                participant_id,
                token
            FROM session_participants
            """;

        List<SessionParticipant> sessionParticipants =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                sessionParticipants.add(
                    new SessionParticipant(
                        result.getInt("id"),
                        result.getInt("game_session_id"),
                        result.getInt("player_id"),
                        result.getString("type"),
                        result.getInt("participant_id"),
                        result.getString("token")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sessionParticipants;
    }

    public void updateSessionParticipant(
        SessionParticipant sessionParticipant
    ) {

        String sql =
            """
            UPDATE session_participants
            SET
                game_session_id = ?,
                player_id = ?,
                type = ?,
                participant_id = ?,
                token = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                sessionParticipant.getGameSessionId()
            );

            statement.setInt(
                2,
                sessionParticipant.getPlayerId()
            );

            statement.setString(
                3,
                sessionParticipant.getType()
            );

            statement.setInt(
                4,
                sessionParticipant.getParticipantId()
            );

            statement.setString(
                5,
                sessionParticipant.getToken()
            );

            statement.setInt(
                6,
                sessionParticipant.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteSessionParticipant(
        int id
    ) {

        String sql =
            """
            DELETE FROM session_participants
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
  //============AREA=============

    public void createArea(
        Area area
    ) {

        String sql =
            """
            INSERT INTO areas (
                id,
                name,
                min_level,
                max_level
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                area.getId()
            );

            statement.setString(
                2,
                area.getName()
            );

            statement.setInt(
                3,
                area.getMinLevel()
            );

            statement.setInt(
                4,
                area.getMaxLevel()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Area findAreaById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                name,
                min_level,
                max_level
            FROM areas
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Area(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getInt("min_level"),
                        result.getInt("max_level")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Area> findAllAreas() {

        String sql =
            """
            SELECT
                id,
                name,
                min_level,
                max_level
            FROM areas
            """;

        List<Area> areas =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                areas.add(
                    new Area(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getInt("min_level"),
                        result.getInt("max_level")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return areas;
    }

    public void updateArea(
        Area area
    ) {

        String sql =
            """
            UPDATE areas
            SET
                name = ?,
                min_level = ?,
                max_level = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setString(
                1,
                area.getName()
            );

            statement.setInt(
                2,
                area.getMinLevel()
            );

            statement.setInt(
                3,
                area.getMaxLevel()
            );

            statement.setInt(
                4,
                area.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteArea(
        int id
    ) {

        String sql =
            """
            DELETE FROM areas
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
  //============AREA POOL=============

    public void createAreaPool(
        AreaPool areaPool
    ) {

        String sql =
            """
            INSERT INTO area_pools (
                id,
                area_id,
                species_id,
                probability
            )
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                areaPool.getId()
            );

            statement.setInt(
                2,
                areaPool.getAreaId()
            );

            statement.setInt(
                3,
                areaPool.getSpeciesId()
            );

            statement.setDouble(
                4,
                areaPool.getProbability()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public AreaPool findAreaPoolById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                area_id,
                species_id,
                probability
            FROM area_pools
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new AreaPool(
                        result.getInt("id"),
                        result.getInt("area_id"),
                        result.getInt("species_id"),
                        result.getDouble("probability")
                    );

                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<AreaPool> findAllAreaPools() {

        String sql =
            """
            SELECT
                id,
                area_id,
                species_id,
                probability
            FROM area_pools
            """;

        List<AreaPool> areaPools =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                areaPools.add(
                    new AreaPool(
                        result.getInt("id"),
                        result.getInt("area_id"),
                        result.getInt("species_id"),
                        result.getDouble("probability")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return areaPools;
    }

    public void updateAreaPool(
        AreaPool areaPool
    ) {

        String sql =
            """
            UPDATE area_pools
            SET
                area_id = ?,
                species_id = ?,
                probability = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                areaPool.getAreaId()
            );

            statement.setInt(
                2,
                areaPool.getSpeciesId()
            );

            statement.setDouble(
                3,
                areaPool.getProbability()
            );

            statement.setInt(
                4,
                areaPool.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAreaPool(
        int id
    ) {

        String sql =
            """
            DELETE FROM area_pools
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============ENCOUNTER=============

    public void createEncounter(
        Encounter encounter
    ) {

        String sql =
            """
            INSERT INTO encounters (
                id,
                game_session_id,
                status
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounter.getId()
            );

            statement.setInt(
                2,
                encounter.getGameSessionId()
            );

            statement.setString(
                3,
                encounter.getStatus()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Encounter findEncounterById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                game_session_id,
                status
            FROM encounters
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new Encounter(
                        result.getInt("id"),
                        result.getInt("game_session_id"),
                        result.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Encounter> findAllEncounters() {

        String sql =
            """
            SELECT
                id,
                game_session_id,
                status
            FROM encounters
            """;

        List<Encounter> encounters =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                encounters.add(
                    new Encounter(
                        result.getInt("id"),
                        result.getInt("game_session_id"),
                        result.getString("status")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encounters;
    }

    public void updateEncounter(
        Encounter encounter
    ) {

        String sql =
            """
            UPDATE encounters
            SET
                game_session_id = ?,
                status = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounter.getGameSessionId()
            );

            statement.setString(
                2,
                encounter.getStatus()
            );

            statement.setInt(
                3,
                encounter.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEncounter(
        int id
    ) {

        String sql =
            """
            DELETE FROM encounters
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //============ENCOUNTER PARTICIPANT=============

    public void createEncounterParticipant(
        EncounterParticipant encounterParticipant
    ) {

        String sql =
            """
            INSERT INTO encounter_participants (
                id,
                encounter_id,
                session_participant_id
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounterParticipant.getId()
            );

            statement.setInt(
                2,
                encounterParticipant.getEncounterId()
            );

            statement.setInt(
                3,
                encounterParticipant.getSessionParticipantId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public EncounterParticipant findEncounterParticipantById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                encounter_id,
                session_participant_id
            FROM encounter_participants
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new EncounterParticipant(
                        result.getInt("id"),
                        result.getInt("encounter_id"),
                        result.getInt("session_participant_id")
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
        return null;
    }

    public List<EncounterParticipant> findAllEncounterParticipants() {

        String sql =
            """
            SELECT
                id,
                encounter_id,
                session_participant_id
            FROM encounter_participants
            """;

        List<EncounterParticipant> encounterParticipants =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                encounterParticipants.add(
                    new EncounterParticipant(
                        result.getInt("id"),
                        result.getInt("encounter_id"),
                        result.getInt("session_participant_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encounterParticipants;
    }

    public void updateEncounterParticipant(
        EncounterParticipant encounterParticipant
    ) {

        String sql =
            """
            UPDATE encounter_participants
            SET
                encounter_id = ?,
                session_participant_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounterParticipant.getEncounterId()
            );

            statement.setInt(
                2,
                encounterParticipant.getSessionParticipantId()
            );

            statement.setInt(
                3,
                encounterParticipant.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEncounterParticipant(
        int id
    ) {

        String sql =
            """
            DELETE FROM encounter_participants
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============ENCOUNTER POKEMON=============

    public void createEncounterPokemon(
        EncounterPokemon encounterPokemon
    ) {

        String sql =
            """
            INSERT INTO encounter_pokemon (
                id,
                encounter_id,
                pokemon_id,
                visibility,
                assigned_participant_id
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounterPokemon.getId()
            );

            statement.setInt(
                2,
                encounterPokemon.getEncounterId()
            );

            statement.setInt(
                3,
                encounterPokemon.getPokemonId()
            );

            statement.setString(
                4,
                encounterPokemon.getVisibility()
            );

            statement.setInt(
                5,
                encounterPokemon.getAssignedParticipantId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public EncounterPokemon findEncounterPokemonById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                encounter_id,
                pokemon_id,
                visibility,
                assigned_participant_id
            FROM encounter_pokemon
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new EncounterPokemon(
                        result.getInt("id"),
                        result.getInt("encounter_id"),
                        result.getInt("pokemon_id"),
                        result.getString("visibility"),
                        result.getInt("assigned_participant_id")
                    );
                }
            }

        } catch (SQLException e) {

            e.printStackTrace();

        }
        return null;
    }

    public List<EncounterPokemon> findAllEncounterPokemons() {

        String sql =
            """
            SELECT
                id,
                encounter_id,
                pokemon_id,
                visibility,
                assigned_participant_id
            FROM encounter_pokemon
            """;

        List<EncounterPokemon> encounterPokemons =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                encounterPokemons.add(
                    new EncounterPokemon(
                        result.getInt("id"),
                        result.getInt("encounter_id"),
                        result.getInt("pokemon_id"),
                        result.getString("visibility"),
                        result.getInt("assigned_participant_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encounterPokemons;
    }

    public void updateEncounterPokemon(
        EncounterPokemon encounterPokemon
    ) {

        String sql =
            """
            UPDATE encounter_pokemon
            SET
                encounter_id = ?,
                pokemon_id = ?,
                visibility = ?,
                assigned_participant_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounterPokemon.getEncounterId()
            );

            statement.setInt(
                2,
                encounterPokemon.getPokemonId()
            );

            statement.setString(
                3,
                encounterPokemon.getVisibility()
            );

            statement.setInt(
                4,
                encounterPokemon.getAssignedParticipantId()
            );

            statement.setInt(
                5,
                encounterPokemon.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEncounterPokemon(
        int id
    ) {

        String sql =
            """
            DELETE FROM encounter_pokemon
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
  //============ENCOUNTER SPAWN=============

    public void createEncounterSpawn(
        EncounterSpawn encounterSpawn
    ) {

        String sql =
            """
            INSERT INTO encounter_spawns (
                id,
                encounter_pokemon_id,
                level,
                gender,
                shiny,
                nature_id,
                ability_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounterSpawn.getId()
            );

            statement.setInt(
                2,
                encounterSpawn.getEncounterPokemonId()
            );

            statement.setInt(
                3,
                encounterSpawn.getLevel()
            );

            statement.setString(
                4,
                encounterSpawn.getGender()
            );

            statement.setBoolean(
                5,
                encounterSpawn.isShiny()
            );

            statement.setInt(
                6,
                encounterSpawn.getNatureId()
            );

            statement.setInt(
                7,
                encounterSpawn.getAbilityId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public EncounterSpawn findEncounterSpawnById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                encounter_pokemon_id,
                level,
                gender,
                shiny,
                nature_id,
                ability_id
            FROM encounter_spawns
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new EncounterSpawn(
                        result.getInt("id"),
                        result.getInt("encounter_pokemon_id"),
                        result.getInt("level"),
                        result.getString("gender"),
                        result.getBoolean("shiny"),
                        result.getInt("nature_id"),
                        result.getInt("ability_id")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EncounterSpawn> findAllEncounterSpawns() {

        String sql =
            """
            SELECT
                id,
                encounter_pokemon_id,
                level,
                gender,
                shiny,
                nature_id,
                ability_id
            FROM encounter_spawns
            """;

        List<EncounterSpawn> encounterSpawns =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                encounterSpawns.add(
                    new EncounterSpawn(
                        result.getInt("id"),
                        result.getInt("encounter_pokemon_id"),
                        result.getInt("level"),
                        result.getString("gender"),
                        result.getBoolean("shiny"),
                        result.getInt("nature_id"),
                        result.getInt("ability_id")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encounterSpawns;
    }

    public void updateEncounterSpawn(
        EncounterSpawn encounterSpawn
    ) {

        String sql =
            """
            UPDATE encounter_spawns
            SET
                encounter_pokemon_id = ?,
                level = ?,
                gender = ?,
                shiny = ?,
                nature_id = ?,
                ability_id = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                encounterSpawn.getEncounterPokemonId()
            );

            statement.setInt(
                2,
                encounterSpawn.getLevel()
            );

            statement.setString(
                3,
                encounterSpawn.getGender()
            );

            statement.setBoolean(
                4,
                encounterSpawn.isShiny()
            );

            statement.setInt(
                5,
                encounterSpawn.getNatureId()
            );

            statement.setInt(
                6,
                encounterSpawn.getAbilityId()
            );

            statement.setInt(
                7,
                encounterSpawn.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteEncounterSpawn(
        int id
    ) {

        String sql =
            """
            DELETE FROM encounter_spawns
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============TRADE OFFER=============

    public void createTradeOffer(
        TradeOffer tradeOffer
    ) {

        String sql =
            """
            INSERT INTO trade_offers (
                id,
                trade_id,
                participant_id,
                pokemon_id,
                item_id,
                quantity
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                tradeOffer.getId()
            );

            statement.setInt(
                2,
                tradeOffer.getTradeId()
            );

            statement.setInt(
                3,
                tradeOffer.getParticipantId()
            );

            statement.setInt(
                4,
                tradeOffer.getPokemonId()
            );

            statement.setInt(
                5,
                tradeOffer.getItemId()
            );

            statement.setInt(
                6,
                tradeOffer.getQuantity()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TradeOffer findTradeOfferById(
        int id
    ) {

        String sql =
            """
            SELECT
                id,
                trade_id,
                participant_id,
                pokemon_id,
                item_id,
                quantity
            FROM trade_offers
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result =
                    statement.executeQuery()) {

                if (result.next()) {

                    return new TradeOffer(
                        result.getInt("id"),
                        result.getInt("trade_id"),
                        result.getInt("participant_id"),
                        result.getInt("pokemon_id"),
                        result.getInt("item_id"),
                        result.getInt("quantity")
                   );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TradeOffer> findAllTradeOffers() {

        String sql =
            """
            SELECT
                id,
                trade_id,
                participant_id,
                pokemon_id,
                item_id,
                quantity
            FROM trade_offers
            """;

        List<TradeOffer> tradeOffers =
            new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql);
             ResultSet result =
                statement.executeQuery()) {

            while (result.next()) {

                tradeOffers.add(
                    new TradeOffer(
                        result.getInt("id"),
                        result.getInt("trade_id"),
                        result.getInt("participant_id"),
                        result.getInt("pokemon_id"),
                        result.getInt("item_id"),
                        result.getInt("quantity")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tradeOffers;
    }

    public void updateTradeOffer(
        TradeOffer tradeOffer
    ) {

        String sql =
            """
            UPDATE trade_offers
            SET
                trade_id = ?,
                participant_id = ?,
                pokemon_id = ?,
                item_id = ?,
                quantity = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(
                1,
                tradeOffer.getTradeId()
            );

            statement.setInt(
                2,
                tradeOffer.getParticipantId()
            );

            statement.setInt(
                3,
                tradeOffer.getPokemonId()
            );

            statement.setInt(
                4,
                tradeOffer.getItemId()
            );

            statement.setInt(
                5,
                tradeOffer.getQuantity()
            );

            statement.setInt(
                6,
                tradeOffer.getId()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTradeOffer(
        int id
    ) {

        String sql =
            """
            DELETE FROM trade_offers
            WHERE id = ?
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
  //============TRADE=============

    public void createTrade(Trade trade) {

        String sql =
            """
            INSERT INTO trades (
                id,
                game_session_id,
                initiator_participant_id,
                receiver_participant_id,
                initiator_confirmed,
                receiver_confirmed,
                status
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            statement.setInt(1, trade.getId());
            statement.setInt(2, trade.getGameSessionId());
            statement.setInt(3, trade.getInitiatorParticipantId());
            statement.setInt(4, trade.getReceiverParticipantId());
            statement.setBoolean(5, trade.isInitiatorConfirmed());
            statement.setBoolean(6, trade.isReceiverConfirmed());
            statement.setString(7, trade.getStatus());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Trade findTradeById(int id) {

        String sql =
            """
            SELECT
                id,
                game_session_id,
                initiator_participant_id,
                receiver_participant_id,
                initiator_confirmed,
                receiver_confirmed,
                status
            FROM trades
            WHERE id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);

            ResultSet result = statement.executeQuery();

            if (result.next()) {

                return new Trade(
                    result.getInt("id"),
                    result.getInt("game_session_id"),
                    result.getInt("initiator_participant_id"),
                    result.getInt("receiver_participant_id"),
                    result.getBoolean("initiator_confirmed"),
                    result.getBoolean("receiver_confirmed"),
                    result.getString("status")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Trade> findAllTrades() {

        String sql =
            """
            SELECT
                id,
                game_session_id,
                initiator_participant_id,
                receiver_participant_id,
                initiator_confirmed,
                receiver_confirmed,
                status
            FROM trades
            """;

        List<Trade> trades = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            ResultSet result = statement.executeQuery();

            while (result.next()) {

                trades.add(
                    new Trade(
                        result.getInt("id"),
                        result.getInt("game_session_id"),
                        result.getInt("initiator_participant_id"),
                        result.getInt("receiver_participant_id"),
                        result.getBoolean("initiator_confirmed"),
                        result.getBoolean("receiver_confirmed"),
                        result.getString("status")
                    )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trades;
    }

    public void updateTrade(Trade trade) {

        String sql =
            """
            UPDATE trades
            SET
                game_session_id = ?,
                initiator_participant_id = ?,
                receiver_participant_id = ?,
                initiator_confirmed = ?,
                receiver_confirmed = ?,
                status = ?
            WHERE id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            statement.setInt(1, trade.getGameSessionId());
            statement.setInt(2, trade.getInitiatorParticipantId());
            statement.setInt(3, trade.getReceiverParticipantId());
            statement.setBoolean(4, trade.isInitiatorConfirmed());
            statement.setBoolean(5, trade.isReceiverConfirmed());
            statement.setString(6, trade.getStatus());
            statement.setInt(7, trade.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTrade(int id) {

        String sql =
            """
            DELETE FROM trades
            WHERE id = ?
            """;

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //============AREA FORMS=============

    // Adds the form to the area, or just changes its weight when it is already there
    public void saveAreaForm(
        int areaId,
        int formId,
        double weight
    ) {

        String sql =
            """
            INSERT INTO area_forms (area_id, form_id, weight)
            VALUES (?, ?, ?)
            ON CONFLICT (area_id, form_id) DO UPDATE SET weight = excluded.weight
            """;

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, areaId);
            statement.setInt(2, formId);
            statement.setDouble(3, weight);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<AreaForm> findAreaFormsOf(
        int areaId
    ) {

        String sql =
            """
            SELECT id, area_id, form_id, weight
            FROM area_forms
            WHERE area_id = ?
            ORDER BY id
            """;

        List<AreaForm> areaForms = new ArrayList<>();

        try (PreparedStatement statement =
                getConnection().prepareStatement(sql)) {

            statement.setInt(1, areaId);

            try (ResultSet result =
                    statement.executeQuery()) {

                while (result.next()) {
                    areaForms.add(
                        new AreaForm(
                            result.getInt("id"),
                            result.getInt("area_id"),
                            result.getInt("form_id"),
                            result.getDouble("weight")
                        )
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return areaForms;
    }

    public void deleteAreaForm(
        int id
    ) {

        try (PreparedStatement statement =
                getConnection().prepareStatement("DELETE FROM area_forms WHERE id = ?")) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAreaFormsOf(
        int areaId
    ) {

        try (PreparedStatement statement =
                getConnection().prepareStatement("DELETE FROM area_forms WHERE area_id = ?")) {

            statement.setInt(1, areaId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
