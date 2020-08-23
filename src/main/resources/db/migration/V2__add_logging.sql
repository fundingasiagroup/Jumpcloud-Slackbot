CREATE TABLE log(
                      id UUID DEFAULT uuid_generate_v1() PRIMARY KEY ,
                      command TEXT NOT NULL,
                      params TEXT NOT NULL
                ) INHERITS (thing);
