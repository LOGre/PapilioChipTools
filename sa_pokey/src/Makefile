TARGET=sa_pokey.dll

sa_pokey.dll: sa_pokey.o Pokey.o Pokeysnd.o
	gcc -shared -o sa_pokey.dll $^

all:	$(TARGET)

clean:
	$(RM) $(TARGET) *.o


# Pokey_SoundInit Pokeysnd.c OK
# Pokey_Process Pokeysnd.c N/A
# Pokey_Initialise Pokey.c OK
# Pokey_GetByte Pokey.c OK
# Pokey_PutByte Pokey.c OK
# Pokey_About sa_pokey.cpp N/A
#
