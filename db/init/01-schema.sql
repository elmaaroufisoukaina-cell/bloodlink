-- BloodLink : schema de la base de donnees (MySQL 8.0)
-- Source de verite du modele de donnees : 11 tables d'entites + 1 table d'association.
-- Ce script est execute automatiquement par docker au premier demarrage (volume vide).

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS parametre_systeme;
DROP TABLE IF EXISTS poche_sanguine;
DROP TABLE IF EXISTS ligne_demande;
DROP TABLE IF EXISTS demande_sang;
DROP TABLE IF EXISTS questionnaire_eligibilite;
DROP TABLE IF EXISTS don;
DROP TABLE IF EXISTS rendez_vous;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS donneur;
DROP TABLE IF EXISTS hopital;
DROP TABLE IF EXISTS compatibilite_groupe;
DROP TABLE IF EXISTS groupe_sanguin;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------
-- groupe_sanguin : les 8 groupes et leur seuil d'alerte (global au reseau)
-- ---------------------------------------------------------------
CREATE TABLE groupe_sanguin (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    code          VARCHAR(3)  NOT NULL,
    seuil_alerte  INT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_groupe_code UNIQUE (code),
    CONSTRAINT ck_groupe_seuil CHECK (seuil_alerte >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- compatibilite_groupe : "le groupe donneur peut donner au groupe receveur"
-- ---------------------------------------------------------------
CREATE TABLE compatibilite_groupe (
    groupe_donneur_id   BIGINT NOT NULL,
    groupe_receveur_id  BIGINT NOT NULL,
    PRIMARY KEY (groupe_donneur_id, groupe_receveur_id),
    CONSTRAINT fk_compat_donneur  FOREIGN KEY (groupe_donneur_id)  REFERENCES groupe_sanguin (id),
    CONSTRAINT fk_compat_receveur FOREIGN KEY (groupe_receveur_id) REFERENCES groupe_sanguin (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- hopital : un hopital du reseau, avec ses horaires et la capacite des creneaux
-- valide = FALSE tant que l'admin systeme n'a pas valide l'hopital
-- ---------------------------------------------------------------
CREATE TABLE hopital (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    nom                   VARCHAR(150) NOT NULL,
    adresse               VARCHAR(255),
    ville                 VARCHAR(100) NOT NULL,
    telephone             VARCHAR(20),
    email                 VARCHAR(150),
    valide                BOOLEAN      NOT NULL DEFAULT FALSE,
    heure_ouverture       TIME         NOT NULL,
    heure_fermeture       TIME         NOT NULL,
    duree_creneau_min     INT          NOT NULL,
    capacite_par_creneau  INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_hopital_horaires CHECK (heure_fermeture > heure_ouverture),
    CONSTRAINT ck_hopital_duree    CHECK (duree_creneau_min > 0),
    CONSTRAINT ck_hopital_capacite CHECK (capacite_par_creneau > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- donneur : donnees du donneur ; date_prochain_don recalculee apres chaque don (R2)
-- ---------------------------------------------------------------
CREATE TABLE donneur (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    cin                VARCHAR(20)   NOT NULL,
    nom                VARCHAR(100)  NOT NULL,
    prenom             VARCHAR(100)  NOT NULL,
    date_naissance     DATE          NOT NULL,
    sexe               ENUM('HOMME','FEMME') NOT NULL,
    poids_kg           DECIMAL(5,2)  NOT NULL,
    telephone          VARCHAR(20),
    ville              VARCHAR(100),
    date_prochain_don  DATE          NULL,
    groupe_sanguin_id  BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_donneur_cin UNIQUE (cin),
    CONSTRAINT ck_donneur_poids CHECK (poids_kg > 0),
    CONSTRAINT fk_donneur_groupe FOREIGN KEY (groupe_sanguin_id) REFERENCES groupe_sanguin (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- utilisateur : un seul compte pour les 5 roles (R13)
--   DONNEUR                                  -> donneur_id renseigne, hopital_id NULL
--   ADMIN_HOPITAL, MEDECIN, GESTIONNAIRE_STOCK -> hopital_id renseigne, donneur_id NULL
--   ADMIN_SYSTEME                            -> les deux NULL
-- ---------------------------------------------------------------
CREATE TABLE utilisateur (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(150) NOT NULL,
    mot_de_passe  VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN_SYSTEME','ADMIN_HOPITAL','MEDECIN','GESTIONNAIRE_STOCK','DONNEUR') NOT NULL,
    actif         BOOLEAN      NOT NULL DEFAULT TRUE,
    donneur_id    BIGINT       NULL,
    hopital_id    BIGINT       NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_utilisateur_email   UNIQUE (email),
    CONSTRAINT uq_utilisateur_donneur UNIQUE (donneur_id),
    CONSTRAINT fk_utilisateur_donneur FOREIGN KEY (donneur_id) REFERENCES donneur (id),
    CONSTRAINT fk_utilisateur_hopital FOREIGN KEY (hopital_id) REFERENCES hopital (id),
    CONSTRAINT ck_utilisateur_role_lien CHECK (
        (role = 'DONNEUR' AND donneur_id IS NOT NULL AND hopital_id IS NULL)
        OR (role IN ('ADMIN_HOPITAL','MEDECIN','GESTIONNAIRE_STOCK') AND hopital_id IS NOT NULL AND donneur_id IS NULL)
        OR (role = 'ADMIN_SYSTEME' AND donneur_id IS NULL AND hopital_id IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- rendez_vous : reservation d'un creneau dans un hopital (R7)
-- ---------------------------------------------------------------
CREATE TABLE rendez_vous (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    date_heure  DATETIME NOT NULL,
    statut      ENUM('PLANIFIE','HONORE','ANNULE','ABSENT') NOT NULL DEFAULT 'PLANIFIE',
    donneur_id  BIGINT   NOT NULL,
    hopital_id  BIGINT   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_rdv_donneur FOREIGN KEY (donneur_id) REFERENCES donneur (id),
    CONSTRAINT fk_rdv_hopital FOREIGN KEY (hopital_id) REFERENCES hopital (id),
    INDEX idx_rdv_hopital_date (hopital_id, date_heure)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- don : un prelevement (REALISE) ou un refus (REFUSE, garde avec son motif, sans poches)
-- medecin_id pointe vers un utilisateur de role MEDECIN
-- ---------------------------------------------------------------
CREATE TABLE don (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    date_don    DATE   NOT NULL,
    type_don    ENUM('SANG_TOTAL','AFERESE') NOT NULL,
    volume_ml   INT    NOT NULL,
    statut      ENUM('REALISE','REFUSE') NOT NULL,
    donneur_id  BIGINT NOT NULL,
    hopital_id  BIGINT NOT NULL,
    medecin_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_don_volume CHECK (volume_ml > 0),
    CONSTRAINT fk_don_donneur FOREIGN KEY (donneur_id) REFERENCES donneur (id),
    CONSTRAINT fk_don_hopital FOREIGN KEY (hopital_id) REFERENCES hopital (id),
    CONSTRAINT fk_don_medecin FOREIGN KEY (medecin_id) REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- questionnaire_eligibilite : 1 pour 1 avec don (composition)
-- motif_refus obligatoire si eligible = FALSE
-- ---------------------------------------------------------------
CREATE TABLE questionnaire_eligibilite (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    poids_kg            DECIMAL(5,2) NOT NULL,
    hemoglobine         DECIMAL(4,1),
    tension_systolique  INT,
    voyage_recent       BOOLEAN      NOT NULL,
    tatouage_recent     BOOLEAN      NOT NULL,
    sous_traitement     BOOLEAN      NOT NULL,
    eligible            BOOLEAN      NOT NULL,
    motif_refus         VARCHAR(255) NULL,
    don_id              BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_questionnaire_don UNIQUE (don_id),
    CONSTRAINT ck_questionnaire_motif CHECK (eligible = TRUE OR motif_refus IS NOT NULL),
    CONSTRAINT fk_questionnaire_don FOREIGN KEY (don_id) REFERENCES don (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- demande_sang : demande d'un hopital (demandeur) a un autre (fournisseur) (R11)
-- ---------------------------------------------------------------
CREATE TABLE demande_sang (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    date_demande            DATETIME     NOT NULL,
    statut                  ENUM('EN_ATTENTE','PARTIELLEMENT_SERVIE','SERVIE','REFUSEE') NOT NULL DEFAULT 'EN_ATTENTE',
    commentaire             VARCHAR(255),
    hopital_demandeur_id    BIGINT       NOT NULL,
    hopital_fournisseur_id  BIGINT       NOT NULL,
    utilisateur_id          BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_demande_hopitaux CHECK (hopital_demandeur_id <> hopital_fournisseur_id),
    CONSTRAINT fk_demande_demandeur   FOREIGN KEY (hopital_demandeur_id)   REFERENCES hopital (id),
    CONSTRAINT fk_demande_fournisseur FOREIGN KEY (hopital_fournisseur_id) REFERENCES hopital (id),
    CONSTRAINT fk_demande_utilisateur FOREIGN KEY (utilisateur_id)         REFERENCES utilisateur (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- ligne_demande : une ligne = un groupe + un produit + une quantite (composition de demande_sang)
-- ---------------------------------------------------------------
CREATE TABLE ligne_demande (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    type_produit       ENUM('GLOBULES_ROUGES','PLASMA','PLAQUETTES') NOT NULL,
    quantite           INT    NOT NULL,
    urgence            ENUM('NORMALE','URGENTE','CRITIQUE') NOT NULL,
    groupe_sanguin_id  BIGINT NOT NULL,
    demande_id         BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_ligne_quantite CHECK (quantite > 0),
    CONSTRAINT fk_ligne_groupe  FOREIGN KEY (groupe_sanguin_id) REFERENCES groupe_sanguin (id),
    CONSTRAINT fk_ligne_demande FOREIGN KEY (demande_id) REFERENCES demande_sang (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- poche_sanguine : produit sanguin issu d'un don (R4, R5, R10)
-- le groupe de la poche = groupe du donneur du don (non duplique)
-- hopital_id = hopital qui detient la poche (ne change pas quand elle est servie)
-- ligne_demande_id renseigne quand la poche est servie
-- ---------------------------------------------------------------
CREATE TABLE poche_sanguine (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    numero_serie      VARCHAR(50) NOT NULL,
    type_produit      ENUM('GLOBULES_ROUGES','PLASMA','PLAQUETTES') NOT NULL,
    volume_ml         INT         NOT NULL,
    date_prelevement  DATE        NOT NULL,
    date_peremption   DATE        NOT NULL,
    statut            ENUM('DISPONIBLE','SERVIE','PERIMEE') NOT NULL DEFAULT 'DISPONIBLE',
    don_id            BIGINT      NOT NULL,
    hopital_id        BIGINT      NOT NULL,
    ligne_demande_id  BIGINT      NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_poche_numero UNIQUE (numero_serie),
    CONSTRAINT ck_poche_volume CHECK (volume_ml > 0),
    CONSTRAINT ck_poche_dates  CHECK (date_peremption > date_prelevement),
    CONSTRAINT fk_poche_don     FOREIGN KEY (don_id)           REFERENCES don (id),
    CONSTRAINT fk_poche_hopital FOREIGN KEY (hopital_id)       REFERENCES hopital (id),
    CONSTRAINT fk_poche_ligne   FOREIGN KEY (ligne_demande_id) REFERENCES ligne_demande (id),
    INDEX idx_poche_stock (hopital_id, statut, date_peremption)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------
-- parametre_systeme : seuils et delais modifiables sans toucher au code (E5)
-- cles : AGE_MIN, AGE_MAX, POIDS_MIN_KG, DELAI_DON_HOMME_JOURS, DELAI_DON_FEMME_JOURS
-- ---------------------------------------------------------------
CREATE TABLE parametre_systeme (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    cle          VARCHAR(100) NOT NULL,
    valeur       VARCHAR(100) NOT NULL,
    description  VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_parametre_cle UNIQUE (cle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
