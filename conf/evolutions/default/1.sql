# --- !Ups

CREATE TABLE "concerts" (
    "id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "date" TEXT NOT NULL,
    "name" TEXT NOT NULL
);

CREATE TABLE "tracks" (
    "id" INTEGER PRIMARY KEY AUTOINCREMENT,
    "name" TEXT NOT NULL,
    "duration" INTEGER NOT NULL,
    "concert_id" INTEGER NOT NULL,
    FOREIGN KEY("concert_id") REFERENCES "concerts"("id")
);

# --- !Downs

DROP TABLE "tracks";
DROP TABLE "concerts";