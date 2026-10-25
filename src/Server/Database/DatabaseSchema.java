package Server.Database;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseSchema {

    private Connection connection;

    public DatabaseSchema(Database database) {
        setConnection(database.getConnection());
    }

    //=========SETTERS==============

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    //==========GETTERS=============

    public Connection getConnection() {
        return connection;
    }

    //=========SCHEMA===============

    public void createSchema() {

        try {

            Statement statement =
                getConnection().createStatement();
            
            //=========PLAYERS===============
            
            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS players (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );
            //=========POKEMON SPECIES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pokemon_species (
            	        dex_number INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );

            //=========POKEMON FORMS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pokemon_forms (
            	        id INTEGER PRIMARY KEY,
            	        species_id INTEGER NOT NULL,
            	        name TEXT NOT NULL,
            	        type1 TEXT NOT NULL,
            	        type2 TEXT,
            	        base_hp INTEGER NOT NULL,
            	        base_attack INTEGER NOT NULL,
            	        base_defense INTEGER NOT NULL,
            	        base_sp_attack INTEGER NOT NULL,
            	        base_sp_defense INTEGER NOT NULL,
            	        base_speed INTEGER NOT NULL,
            	        cry_id INTEGER NOT NULL,

            	        CHECK (length(trim(name)) > 0),

            	        CHECK (
            	            base_hp >= 0
            	            AND base_attack >= 0
            	            AND base_defense >= 0
            	            AND base_sp_attack >= 0
            	            AND base_sp_defense >= 0
            	            AND base_speed >= 0
            	        ),

            	        CHECK (
            	            type1 IN (
            	                'Normal',
            	                'Fire',
            	                'Water',
            	                'Electric',
            	                'Grass',
            	                'Ice',
            	                'Fighting',
            	                'Poison',
            	                'Ground',
            	                'Flying',
            	                'Psychic',
            	                'Bug',
            	                'Rock',
            	                'Ghost',
            	                'Dragon',
            	                'Dark',
            	                'Steel',
            	                'Fairy'
            	            )
            	        ),

            	        CHECK (
            	            type2 IS NULL
            	            OR type2 IN (
            	                'Normal',
            	                'Fire',
            	                'Water',
            	                'Electric',
            	                'Grass',
            	                'Ice',
            	                'Fighting',
            	                'Poison',
            	                'Ground',
            	                'Flying',
            	                'Psychic',
            	                'Bug',
            	                'Rock',
            	                'Ghost',
            	                'Dragon',
            	                'Dark',
            	                'Steel',
            	                'Fairy'
            	            )
            	        ),

            	        CHECK (cry_id > 0),

            	        FOREIGN KEY (species_id)
            	            REFERENCES pokemon_species(dex_number)
            	    )
            	    """
            );

            //=========POKEMON SPRITES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pokemon_sprites (
            	        id INTEGER PRIMARY KEY,
            	        form_id INTEGER NOT NULL,
            	        normal_sprite TEXT NOT NULL,
            	        shiny_sprite TEXT NOT NULL,

            	        CHECK (
            	            length(trim(normal_sprite)) > 0
            	            AND length(trim(shiny_sprite)) > 0
            	        ),

            	        FOREIGN KEY (form_id)
            	            REFERENCES pokemon_forms(id)
            	    )
            	    """
            );

            //=========EVOLUTIONS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS evolutions (
            	        id INTEGER PRIMARY KEY,
            	        from_species_id INTEGER NOT NULL,
            	        from_form_id INTEGER NOT NULL,
            	        to_species_id INTEGER NOT NULL,
            	        to_form_id INTEGER NOT NULL,

            	        CHECK (from_form_id <> to_form_id),

            	        FOREIGN KEY (from_species_id)
            	            REFERENCES pokemon_species(dex_number),
            	        FOREIGN KEY (from_form_id)
            	            REFERENCES pokemon_forms(id),
            	        FOREIGN KEY (to_species_id)
            	            REFERENCES pokemon_species(dex_number),
            	        FOREIGN KEY (to_form_id)
            	            REFERENCES pokemon_forms(id)
            	    )
            	    """
            );
            
          //=========EVOLUTION CONDITIONS===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS evolution_conditions (
                    id INTEGER PRIMARY KEY,
                    evolution_id INTEGER NOT NULL,
                    condition_type TEXT NOT NULL,
                    value TEXT NOT NULL,
                    FOREIGN KEY (evolution_id)
                        REFERENCES evolutions(id)
                )
                """
            );
            
          //=========ABILITIES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS abilities (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        description TEXT NOT NULL,

            	        CHECK (
            	            length(trim(name)) > 0
            	            AND length(trim(description)) > 0
            	        )
            	    )
            	    """
            );
            
          //=========FORM ABILITIES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS form_abilities (
            	        form_id INTEGER NOT NULL,
            	        ability_id INTEGER NOT NULL,
            	        is_hidden INTEGER NOT NULL,

            	        CHECK (is_hidden IN (0, 1)),

            	        PRIMARY KEY (form_id, ability_id),

            	        FOREIGN KEY (form_id)
            	            REFERENCES pokemon_forms(id),
            	        FOREIGN KEY (ability_id)
            	            REFERENCES abilities(id)
            	    )
            	    """
            );
            
          //=========NATURES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS natures (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        increased_attribute TEXT NOT NULL,
            	        decreased_attribute TEXT NOT NULL,

            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );
            
          //=========MOVES===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS moves (
                    id INTEGER PRIMARY KEY,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    accuracy INTEGER NOT NULL,
                    move_class TEXT NOT NULL,
                    range TEXT NOT NULL,
                    damage TEXT,
                    description TEXT NOT NULL
                )
                """
            );
            
          //=========SKILLS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS skills (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );
            
          //=========FORM SKILLS===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS form_skills (
                    form_id INTEGER NOT NULL,
                    skill_id INTEGER NOT NULL,
                    PRIMARY KEY (form_id, skill_id),
                    FOREIGN KEY (form_id)
                        REFERENCES pokemon_forms(id),
                    FOREIGN KEY (skill_id)
                        REFERENCES skills(id)
                )
                """
            );
            
          //=========CAPABILITIES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS capabilities (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );
            
          //=========FORM CAPABILITIES===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS form_capabilities (
                    form_id INTEGER NOT NULL,
                    capability_id INTEGER NOT NULL,
                    value TEXT NOT NULL,
                    PRIMARY KEY (form_id, capability_id),
                    FOREIGN KEY (form_id)
                        REFERENCES pokemon_forms(id),
                    FOREIGN KEY (capability_id)
                        REFERENCES capabilities(id)
                )
                """
            );
            
            //=========ITEMS===============
            
            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS items (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        description TEXT NOT NULL,
            	        category TEXT NOT NULL,
            	        effect TEXT NOT NULL,

            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );
            
            
          //=========TRAINERS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS trainers (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        level INTEGER NOT NULL,
            	        hp INTEGER NOT NULL,
            	        money INTEGER NOT NULL,
            	        player_id INTEGER NOT NULL,

            	        CHECK (length(trim(name)) > 0),
            	        CHECK (level BETWEEN 1 AND 50),
            	        CHECK (hp >= 0),
            	        CHECK (money >= 0),

            	        FOREIGN KEY (player_id)
            	            REFERENCES players(id)
            	    )
            	    """
            );
            
            
            //=========MASTERS===============
            
            statement.executeUpdate(
                """
            		CREATE TABLE IF NOT EXISTS masters (
            				id INTEGER PRIMARY KEY,
            				player_id INTEGER NOT NULL,
            				FOREIGN KEY (player_id)
            				REFERENCES players(id)
            				)
                """
            );
            
            
          //=========PCS===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS pcs (
                    id INTEGER PRIMARY KEY,
                    trainer_id INTEGER NOT NULL,
                    FOREIGN KEY (trainer_id)
                        REFERENCES trainers(id)
                )
                """
            );
            
          //=========PC BOXES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pc_boxes (
            	        id INTEGER PRIMARY KEY,
            	        pc_id INTEGER NOT NULL,
            	        name TEXT NOT NULL,
            	        box_number INTEGER NOT NULL,
            	        slot_limit INTEGER NOT NULL,

            	        CHECK (length(trim(name)) > 0),
            	        CHECK (box_number >= 1),
            	        CHECK (slot_limit BETWEEN 1 AND 30),

            	        FOREIGN KEY (pc_id)
            	            REFERENCES pcs(id)
            	    )
            	    """
            );
            
          //=========TRAINER INVENTORY===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS trainer_inventory (
            	        trainer_id INTEGER NOT NULL,
            	        item_id INTEGER NOT NULL,
            	        quantity INTEGER NOT NULL,

            	        CHECK (quantity > 0),

            	        PRIMARY KEY (trainer_id, item_id),

            	        FOREIGN KEY (trainer_id)
            	            REFERENCES trainers(id),

            	        FOREIGN KEY (item_id)
            	            REFERENCES items(id)
            	    )
            	    """
            	);
            
            
            
          //=========POKEMON===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pokemon (
            	        id INTEGER PRIMARY KEY,
            	        trainer_id INTEGER,
            	        species_id INTEGER NOT NULL,
            	        form_id INTEGER NOT NULL,
            	        nickname TEXT,
            	        gender TEXT NOT NULL,
            	        shiny INTEGER NOT NULL,
            	        nature_id INTEGER NOT NULL,
            	        ability_id INTEGER NOT NULL,
            	        level INTEGER NOT NULL,
            	        xp INTEGER NOT NULL,
            	        hp REAL NOT NULL,
            	        undistributed_attribute_points INTEGER NOT NULL,
            	        held_item_id INTEGER NOT NULL,
            	        party_slot INTEGER NOT NULL,
            	        pc_box_id INTEGER NOT NULL,
            	        pc_slot INTEGER NOT NULL,
            	        captured_at INTEGER NOT NULL,

            	        CHECK (
            	            nickname IS NULL
            	            OR length(trim(nickname)) > 0
            	        ),

            	        CHECK (
            	            gender IN (
            	                'Male',
            	                'Female',
            	                'Genderless'
            	            )
            	        ),

            	        CHECK (shiny IN (0, 1)),
            	        CHECK (level BETWEEN 1 AND 100),
            	        CHECK (xp >= 0),
            	        CHECK (hp >= 0),
            	        CHECK (undistributed_attribute_points >= 0),
            	        CHECK (held_item_id >= 0),
            	        CHECK (party_slot BETWEEN 0 AND 6),
            	        CHECK (pc_box_id >= 0),
            	        CHECK (pc_slot >= 0),

            	        FOREIGN KEY (trainer_id)
            	            REFERENCES trainers(id),

            	        FOREIGN KEY (species_id)
            	            REFERENCES pokemon_species(dex_number),

            	        FOREIGN KEY (form_id)
            	            REFERENCES pokemon_forms(id),

            	        FOREIGN KEY (nature_id)
            	            REFERENCES natures(id),

            	        FOREIGN KEY (ability_id)
            	            REFERENCES abilities(id)
            	    )
            	    """
            );
            
          //=========POKEMON ATTRIBUTE POINTS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pokemon_attribute_points (
            	        pokemon_id INTEGER NOT NULL,
            	        attribute TEXT NOT NULL,
            	        points INTEGER NOT NULL,

            	        CHECK (
            	            attribute IN (
            	                'HP',
            	                'Attack',
            	                'Defense',
            	                'Special Attack',
            	                'Special Defense',
            	                'Speed'
            	            )
            	        ),

            	        CHECK (points >= 0),

            	        PRIMARY KEY (pokemon_id, attribute),

            	        FOREIGN KEY (pokemon_id)
            	            REFERENCES pokemon(id)
            	    )
            	    """
            );
            
          //=========CHARACTER SHEETS (made before any session) + where they were placed===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS character_sheets (
            	        id INTEGER PRIMARY KEY,
            	        owner TEXT NOT NULL,
            	        name TEXT NOT NULL,
            	        CHECK (length(trim(name)) > 0)
            	    )
            	    """
            );

            // trainer_id = the independent copy inside a session; sheet_id = the sheet it came from
            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS sheet_placements (
            	        trainer_id INTEGER PRIMARY KEY,
            	        sheet_id INTEGER NOT NULL
            	    )
            	    """
            );

          //=========POKEMON STAT EXCEPTIONS (stats that ignore the Base Relation rule)===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS pokemon_stat_exceptions (
                    pokemon_id INTEGER NOT NULL,
                    attribute TEXT NOT NULL,

                    CHECK (
                        attribute IN (
                            'HP',
                            'Attack',
                            'Defense',
                            'Special Attack',
                            'Special Defense',
                            'Speed'
                        )
                    ),

                    PRIMARY KEY (pokemon_id, attribute),

                    FOREIGN KEY (pokemon_id)
                        REFERENCES pokemon(id)
                )
                """
            );


          //=========TRAINER ATTRIBUTE POINTS===============
            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS trainer_attribute_points (
            	        trainer_id INTEGER NOT NULL,
            	        attribute TEXT NOT NULL,
            	        points INTEGER NOT NULL,
            	        PRIMARY KEY (trainer_id, attribute),
            	        CHECK (
            	            attribute IN (
            	                'HP',
            	                'Attack',
            	                'Defense',
            	                'Special Attack',
            	                'Special Defense',
            	                'Speed'
            	            )
            	        ),
            	        CHECK (points >= 0),
            	        FOREIGN KEY (trainer_id)
            	            REFERENCES trainers(id)
            	    )
            	    """
            	);
            
            
            
          //=========POKEMON VITAMINS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS pokemon_vitamins (
            	        pokemon_id INTEGER NOT NULL,
            	        vitamin_id INTEGER NOT NULL,
            	        quantity INTEGER NOT NULL,

            	        CHECK (quantity BETWEEN 0 AND 5),

            	        PRIMARY KEY (pokemon_id, vitamin_id),

            	        FOREIGN KEY (pokemon_id)
            	            REFERENCES pokemon(id),

            	        FOREIGN KEY (vitamin_id)
            	            REFERENCES items(id)
            	    )
            	    """
            );
            
            
            //=========POKEMON CAPABILITIES===============
            
            statement.executeUpdate(
                """
            		CREATE TABLE IF NOT EXISTS pokemon_capabilities (
            				pokemon_id INTEGER NOT NULL,
            				capability_id INTEGER NOT NULL,
            				modifier INTEGER NOT NULL,
            				PRIMARY KEY (pokemon_id, capability_id),
            				FOREIGN KEY (pokemon_id)
            				REFERENCES pokemon(id),
            				FOREIGN KEY (capability_id)
            				REFERENCES capabilities(id)
            				)
                """
            );
            
            //=========POKEMON MOVES===============
            
            statement.executeUpdate(
                """
            		CREATE TABLE IF NOT EXISTS pokemon_moves (
            				pokemon_id INTEGER NOT NULL,
            				move_id INTEGER NOT NULL,
            				PRIMARY KEY (pokemon_id, move_id),
            				FOREIGN KEY (pokemon_id)
            				REFERENCES pokemon(id),
            				FOREIGN KEY (move_id)
            				REFERENCES moves(id)
            				)
                """
            );
            
            //=========POKEMON SKILLS===============
            
            statement.executeUpdate(
                """
            		CREATE TABLE IF NOT EXISTS pokemon_skills (
            				pokemon_id INTEGER NOT NULL,
            				skill_id INTEGER NOT NULL,
            				modifier INTEGER NOT NULL,
            				PRIMARY KEY (pokemon_id, skill_id),
            				FOREIGN KEY (pokemon_id)
            				REFERENCES pokemon(id),
            				FOREIGN KEY (skill_id)
            				REFERENCES skills(id)
            				)
                """
            );
            
          //=========POKEMON CONDITIONS===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS pokemon_conditions (
                    id INTEGER PRIMARY KEY,
                    pokemon_id INTEGER NOT NULL,
                    condition TEXT NOT NULL,
                    FOREIGN KEY (pokemon_id)
                        REFERENCES pokemon(id)
                )
                """
            );
            
          //=========INJURIES===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS injuries (
                    id INTEGER PRIMARY KEY,
                    created_at INTEGER NOT NULL,
                    owner_type TEXT NOT NULL,
                    owner_id INTEGER NOT NULL,
                    removed_at INTEGER
                )
                """
            );
            
          //=========GAME SESSIONS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS game_sessions (
            	        id INTEGER PRIMARY KEY,
            	        host_player_id INTEGER NOT NULL,
            	        current_day INTEGER NOT NULL,
            	        join_code TEXT NOT NULL UNIQUE,

            	        CHECK (current_day >= 1),

            	        FOREIGN KEY (host_player_id)
            	            REFERENCES players(id)
            	    )
            	    """
            );
            
            
          //=========SESSION PARTICIPANTS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS session_participants (
            	        id INTEGER PRIMARY KEY,
            	        game_session_id INTEGER NOT NULL,
            	        player_id INTEGER NOT NULL,
            	        type TEXT NOT NULL,
            	        participant_id INTEGER NOT NULL,
            	        token TEXT,

            	        CHECK (
            	            type IN (
            	                'Master',
            	                'Player'
            	            )
            	        ),

            	        FOREIGN KEY (game_session_id)
            	            REFERENCES game_sessions(id),

            	        FOREIGN KEY (player_id)
            	            REFERENCES players(id)
            	    )
            	    """
            );
            
            //=========AREAS===============
            
            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS areas (
            	        id INTEGER PRIMARY KEY,
            	        name TEXT NOT NULL,
            	        min_level INTEGER NOT NULL,
            	        max_level INTEGER NOT NULL,

            	        CHECK (length(trim(name)) > 0),

            	        CHECK (
            	            min_level BETWEEN 1 AND 100
            	            AND max_level BETWEEN 1 AND 100
            	            AND min_level <= max_level
            	        )
            	    )
            	    """
            );
            
            
            //=========AREA POOLS===============
            
            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS area_pools (
            	        id INTEGER PRIMARY KEY,
            	        area_id INTEGER NOT NULL,
            	        species_id INTEGER NOT NULL,
            	        probability REAL NOT NULL,

            	        CHECK (probability >= 0),

            	        FOREIGN KEY (area_id)
            	            REFERENCES areas(id),

            	        FOREIGN KEY (species_id)
            	            REFERENCES pokemon_species(dex_number)
            	    )
            	    """
            );
            
            //=========AREA FORMS (what can appear in an area, with a relative weight)===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS area_forms (
            	        id INTEGER PRIMARY KEY,
            	        area_id INTEGER NOT NULL,
            	        form_id INTEGER NOT NULL,
            	        weight REAL NOT NULL,

            	        CHECK (weight > 0),

            	        UNIQUE (area_id, form_id),

            	        FOREIGN KEY (area_id)
            	            REFERENCES areas(id),

            	        FOREIGN KEY (form_id)
            	            REFERENCES pokemon_forms(id)
            	    )
            	    """
            );

          //=========ENCOUNTERS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS encounters (
            	        id INTEGER PRIMARY KEY,
            	        game_session_id INTEGER NOT NULL,
            	        status TEXT NOT NULL,

            	        CHECK (
            	            status IN (
            	                'Active',
            	                'Finished',
            	                'Cancelled'
            	            )
            	        ),

            	        FOREIGN KEY (game_session_id)
            	            REFERENCES game_sessions(id)
            	    )
            	    """
            );
            
          //=========ENCOUNTER PARTICIPANTS===============

            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS encounter_participants (
                    id INTEGER PRIMARY KEY,
                    encounter_id INTEGER NOT NULL,
                    session_participant_id INTEGER NOT NULL,
                    FOREIGN KEY (encounter_id)
                        REFERENCES encounters(id),
                    FOREIGN KEY (session_participant_id)
                        REFERENCES session_participants(id)
                )
                """
            );
            
          //=========ENCOUNTER POKEMON===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS encounter_pokemon (
            	        id INTEGER PRIMARY KEY,
            	        encounter_id INTEGER NOT NULL,
            	        assigned_participant_id INTEGER NOT NULL,

            	        CHECK (assigned_participant_id >= 0),

            	        FOREIGN KEY (encounter_id)
            	            REFERENCES encounters(id)
            	    )
            	    """
            );
            
          //=========ENCOUNTER SPAWNS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS encounter_spawns (
            	        id INTEGER PRIMARY KEY,
            	        encounter_pokemon_id INTEGER NOT NULL,
            	        level INTEGER NOT NULL,
            	        gender TEXT NOT NULL,
            	        shiny INTEGER NOT NULL,
            	        nature_id INTEGER NOT NULL,
            	        ability_id INTEGER NOT NULL,

            	        CHECK (level BETWEEN 1 AND 100),

            	        CHECK (
            	            gender IN (
            	                'Male',
            	                'Female',
            	                'Genderless'
            	            )
            	        ),

            	        CHECK (shiny IN (0, 1)),

            	        FOREIGN KEY (encounter_pokemon_id)
            	            REFERENCES encounter_pokemon(id),

            	        FOREIGN KEY (nature_id)
            	            REFERENCES natures(id),

            	        FOREIGN KEY (ability_id)
            	            REFERENCES abilities(id)
            	    )
            	    """
            );
            
            
          //=========TRADES===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS trades (
            	        id INTEGER PRIMARY KEY,
            	        game_session_id INTEGER NOT NULL,
            	        initiator_participant_id INTEGER NOT NULL,
            	        receiver_participant_id INTEGER NOT NULL,
            	        initiator_confirmed INTEGER NOT NULL,
            	        receiver_confirmed INTEGER NOT NULL,
            	        status TEXT NOT NULL,

            	        CHECK (initiator_confirmed IN (0, 1)),

            	        CHECK (receiver_confirmed IN (0, 1)),

            	        CHECK (
            	            status IN (
            	                'Pending',
            	                'Confirmed',
            	                'Completed',
            	                'Cancelled'
            	            )
            	        ),

            	        FOREIGN KEY (game_session_id)
            	            REFERENCES game_sessions(id),

            	        FOREIGN KEY (initiator_participant_id)
            	            REFERENCES session_participants(id),

            	        FOREIGN KEY (receiver_participant_id)
            	            REFERENCES session_participants(id)
            	    )
            	    """
            );
            
          //=========TRADE OFFERS===============

            statement.executeUpdate(
            	    """
            	    CREATE TABLE IF NOT EXISTS trade_offers (
            	        id INTEGER PRIMARY KEY,
            	        trade_id INTEGER NOT NULL,
            	        participant_id INTEGER NOT NULL,
            	        pokemon_id INTEGER,
            	        item_id INTEGER,
            	        quantity INTEGER NOT NULL,

            	        CHECK (quantity > 0),

            	        CHECK (
            	            (pokemon_id IS NOT NULL AND item_id IS NULL)
            	            OR
            	            (pokemon_id IS NULL AND item_id IS NOT NULL)
            	        ),

            	        FOREIGN KEY (trade_id)
            	            REFERENCES trades(id),

            	        FOREIGN KEY (participant_id)
            	            REFERENCES session_participants(id),

            	        FOREIGN KEY (pokemon_id)
            	            REFERENCES pokemon(id),

            	        FOREIGN KEY (item_id)
            	            REFERENCES items(id)
            	    )
            	    """
            	);
        
            statement.close();

            System.out.println("Database schema created!");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}