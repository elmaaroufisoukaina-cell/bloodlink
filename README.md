# BloodLink

> Gestion des dons et des stocks de sang dans un réseau d'hôpitaux.
> Projet de fin de module JEE, ENSAM Casablanca, IAGI.

## Présentation

BloodLink relie les donneurs et les hôpitaux d'un même réseau :

- les **donneurs** s'inscrivent, choisissent un hôpital et réservent un rendez-vous ;
- les **hôpitaux** partagent la visibilité de leurs stocks et peuvent se demander des poches ;
- le **système** vérifie l'éligibilité, suit chaque poche du don jusqu'à l'hôpital qui la reçoit,
  périme automatiquement les poches expirées et signale les pénuries.

## Démarrage rapide

**Prérequis :** JDK 21 (IntelliJ IDEA peut le télécharger), Docker Desktop (avec WSL 2 sous Windows), Git.

```
git clone https://github.com/elmaaroufisoukaina-cell/bloodlink.git
cd bloodlink
docker compose up -d
```

La base de données démarre, et les tables et données de test sont créées automatiquement
depuis `db/init/01-schema.sql` et `db/init/02-data.sql`.

Lancez ensuite l'application :

- depuis IntelliJ : classe `BloodlinkApplication` ;
- ou en ligne de commande : `./mvnw spring-boot:run` (sous Windows : `mvnw.cmd spring-boot:run`).

Puis ouvrez **http://localhost:8080**.

## Comptes de test

Mot de passe commun : `Bloodlink123`

| Rôle | E-mail |
|---|---|
| Admin système | admin@bloodlink.ma |
| Admin hôpital (CHU Ibn Rochd, Casablanca) | admin.casa@bloodlink.ma |
| Admin hôpital (Rabat) | admin.rabat@bloodlink.ma |
| Admin hôpital (Marrakech) | admin.marrakech@bloodlink.ma |
| Médecin (Casablanca) | medecin.casa@bloodlink.ma |
| Gestionnaire de stock (Casablanca) | stock.casa@bloodlink.ma |
| Donneur éligible (O-) | rachid.sekkat@mail.ma |
| Donneur non éligible (don récent) | youssef.alami@mail.ma |

## Scénario de démonstration

1. **Tableau de bord** (admin système) : jauges par groupe, O- sous son seuil.
2. **Alerte de pénurie** (gestionnaire de stock) : liste des donneurs O- éligibles à contacter.
3. **Demande entre hôpitaux** : l'admin de Rabat envoie une demande à Casablanca, l'admin de Casablanca
   l'accepte, les poches les plus proches de la péremption sont proposées en premier, le statut est mis à jour.
4. **Traçabilité** : d'une poche jusqu'à l'hôpital destinataire, et côté donneur
   « Votre don a été utilisé à l'hôpital X ».

## Architecture technique

**Technologies :** Java 21, Spring Boot 4 (Web MVC, Data JPA, Security, Validation), Thymeleaf,
Bootstrap 5, MySQL 8.0 (Docker), Maven, Lombok.

Architecture en couches, paquet `com.ensam.bloodlink` :

| Paquet | Rôle |
|---|---|
| `entity` | entités JPA et énumérations |
| `repository` | Spring Data JPA, requêtes dédiées |
| `service` | règles métier : éligibilité, dons et poches, rendez-vous, demandes entre hôpitaux, alerte, traçabilité |
| `controller` | contrôleurs Spring MVC |
| `dto` | formulaires |
| `config` | Spring Security (5 rôles, BCrypt) |
| `exception` | exceptions métier et gestion centralisée des erreurs |

Le schéma SQL (`db/init/01-schema.sql`) est la source de vérité : Hibernate ne modifie pas les tables.

## Problèmes fréquents

### Le port 3306 est déjà utilisé

Ne modifiez pas les fichiers partagés. Créez à la racine un fichier `docker-compose.override.yml`
(déjà ignoré par Git) :

```yaml
services:
  db:
    ports: !override
      - "3307:3306"
```

Puis, dans IntelliJ (Run > Edit Configurations > Environment variables), ajoutez :

```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3307/bloodlink?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

### Repartir d'une base vide

Les scripts SQL ne s'exécutent que sur une base vide :

```
docker compose down -v
docker compose up -d
```

## Équipe

Wiam El Hamoudi et Soukaina Elmaaroufi