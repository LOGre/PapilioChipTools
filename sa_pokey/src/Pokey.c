#include <stdio.h>
#include <inttypes.h>

#include "rs232.h"


/* Crippled version for the sa_pokey.dll use only */
/* #include <stdlib.h> - was here only for rand() ? */
#ifdef VMS
#include <types.h>
#else
#include <sys/types.h>
#endif
#include <time.h>

//#include "atari.h"
//#include "cpu.h"
//#include "pia.h"
#include "pokey.h"
//#include "gtia.h"
//#include "sio.h"
//#include "statesav.h"
#include "pokeysnd.h"
//#include "input.h"

#ifdef __PLUS
#include "config.h"
#include "sound_win.h"
#endif /*__PLUS*/

#ifdef SERIO_SOUND
void Update_serio_sound( int out, UBYTE data );
#endif

#ifdef POKEY_UPDATE
extern void pokey_update(void);
#endif

#ifndef NO_VOL_ONLY
void Update_vol_only_sound( void );
#endif  /* NO_VOL_ONLY */

UBYTE KBCODE;
UBYTE SERIN;
UBYTE IRQST;
UBYTE IRQEN;
UBYTE SKSTAT;
UBYTE SKCTLS;
int DELAYED_SERIN_IRQ;
int DELAYED_SEROUT_IRQ;
int DELAYED_XMTDONE_IRQ;

/* structures to hold the 9 pokey control bytes */
UBYTE AUDF[4 * MAXPOKEYS];	/* AUDFx (D200, D202, D204, D206) */
UBYTE AUDC[4 * MAXPOKEYS];	/* AUDCx (D201, D203, D205, D207) */
UBYTE AUDCTL[MAXPOKEYS];	/* AUDCTL (D208) */
int DivNIRQ[4], DivNMax[4];
ULONG Base_mult[MAXPOKEYS];		/* selects either 64Khz or 15Khz clock mult */

UBYTE POT_input[8] = {228, 228, 228, 228, 228, 228, 228, 228};
static int pot_scanline;

#ifdef STEREO
#ifdef __PLUS
int stereo_enabled = FALSE;
#else /*__PLUS*/
int stereo_enabled = TRUE;
#endif /*__PLUS*/
#endif

UBYTE poly9_lookup[511];
UBYTE poly17_lookup[16385];
static ULONG random_scanline_counter;

#ifdef __PLUS
ULONG POKEY_GetRandomCounter( void )
{
	return random_scanline_counter;
}

void POKEY_SetRandomCounter( ULONG ulValue )
{
	random_scanline_counter = ulValue;
}
#endif /*__PLUS*/

UBYTE Pokey_GetByte(UWORD addr)
{
	UBYTE byte = 0xff;

	extern FILE *fout;
	fprintf( fout, "%s: addr=%" PRIx16 "\n", __func__, addr);
#ifdef STEREO
	if (addr & 0x0010 && stereo_enabled)
		return 0;
#endif
	addr &= 0x0f;
	if (addr < 8) {
		byte = POT_input[addr];
		if (byte <= pot_scanline)
			return byte;
		return pot_scanline;
	}
	switch (addr) {
	case _ALLPOT:
		{
			int i;
			for (i = 0; i < 8; i++)
				if (POT_input[addr] <= pot_scanline)
					byte &= ~(1 << i);		/* reset bit if pot value known */
		}
		break;
	case _KBCODE:
		byte = KBCODE;
		break;
/*!	case _RANDOM:
		if ((SKCTLS & 0x03) != 0) {
			int i = random_scanline_counter + xpos;
			if (AUDCTL[0] & POLY9)
				byte = poly9_lookup[i % POLY9_SIZE];
			else {
				UBYTE *ptr;
				i %= POLY17_SIZE;
				ptr = poly17_lookup + (i >> 3);
				i &= 7;
				byte = (UBYTE) ((ptr[0] >> i) | (ptr[1] << (8 - i)));
			}
		}
		break;
*/
	case _SERIN:
		byte = SERIN;
#ifdef SERIO_SOUND
			Update_serio_sound(0,byte);
#endif
		break;
	case _IRQST:
		byte = IRQST;
		break;
	case _SKSTAT:
		byte = SKSTAT;
		break;
	}

	return byte;
}

void Update_Counter(int chan_mask);

int POKEY_siocheck(void)
{
 	return (AUDF[CHAN3] == 0x28 || AUDF[CHAN3] == 0x08 || AUDF[CHAN3] == 0x0a)
 		&& AUDF[CHAN4] == 0x00 && (AUDCTL[0] & 0x28) == 0x28;
}

#ifdef __PLUS
#define SOUND_GAIN 1
#else /*__PLUS*/
#define SOUND_GAIN 2
#endif /*__PLUS*/

extern int port;
void Pokey_PutByte(UWORD addr, UBYTE byte)
{
	if (addr < 0x10)
	{
		SendByte( port, addr);
		SendByte( port, byte);
	}
	return;
/*
	extern FILE *fout;
#define MAXR 256
	static UBYTE rvalid[MAXR];
	static UBYTE rvalue[MAXR];
	if (!rvalid[addr & 0xff] || rvalue[addr & 0xff] != byte)
		fprintf( fout, "%s: addr=%" PRIx16 " byte=%" PRIx8 "\n", __func__, addr, byte);
	rvalid[addr & 0xff] = 1;
	rvalue[addr & 0xff] = byte;
*/
#ifdef STEREO
	addr &= stereo_enabled ? 0x1f : 0x0f;
#else
	addr &= 0x0f;
#endif
	switch (addr) {
	case _AUDC1:
		AUDC[CHAN1] = byte;
		Update_pokey_sound(_AUDC1, byte, 0, SOUND_GAIN);
		break;
	case _AUDC2:
		AUDC[CHAN2] = byte;
		Update_pokey_sound(_AUDC2, byte, 0, SOUND_GAIN);
		break;
	case _AUDC3:
		AUDC[CHAN3] = byte;
		Update_pokey_sound(_AUDC3, byte, 0, SOUND_GAIN);
		break;
	case _AUDC4:
		AUDC[CHAN4] = byte;
		Update_pokey_sound(_AUDC4, byte, 0, SOUND_GAIN);
		break;
	case _AUDCTL:
		AUDCTL[0] = byte;

		/* determine the base multiplier for the 'div by n' calculations */
		if (byte & CLOCK_15)
			Base_mult[0] = DIV_15;
		else
			Base_mult[0] = DIV_64;

		Update_Counter((1 << CHAN1) | (1 << CHAN2) | (1 << CHAN3) | (1 << CHAN4));
		Update_pokey_sound(_AUDCTL, byte, 0, SOUND_GAIN);
		break;
	case _AUDF1:
		AUDF[CHAN1] = byte;
		Update_Counter((AUDCTL[0] & CH1_CH2) ? ((1 << CHAN2) | (1 << CHAN1)) : (1 << CHAN1));
		Update_pokey_sound(_AUDF1, byte, 0, SOUND_GAIN);
		break;
	case _AUDF2:
		AUDF[CHAN2] = byte;
		Update_Counter(1 << CHAN2);
		Update_pokey_sound(_AUDF2, byte, 0, SOUND_GAIN);
		break;
	case _AUDF3:
		AUDF[CHAN3] = byte;
		Update_Counter((AUDCTL[0] & CH3_CH4) ? ((1 << CHAN4) | (1 << CHAN3)) : (1 << CHAN3));
		Update_pokey_sound(_AUDF3, byte, 0, SOUND_GAIN);
		break;
	case _AUDF4:
		AUDF[CHAN4] = byte;
		Update_Counter(1 << CHAN4);
		Update_pokey_sound(_AUDF4, byte, 0, SOUND_GAIN);
		break;
/*!	case _IRQEN:
		IRQEN = byte;
#ifdef DEBUG1
		printf("WR: IRQEN = %x, PC = %x\n", IRQEN, PC);
#endif
		IRQST |= ~byte & 0xf7;	// Reset disabled IRQs except XMTDONE
		if ((~IRQST & IRQEN) == 0)
			IRQ = 0;
		break;

	case _SKRES:
		SKSTAT |= 0xe0;
		break;
	case _POTGO:
		if (!(SKCTLS & 4))
			pot_scanline = 0;	// slow pot mode 
		break;
	case _SEROUT:
		if ((SKCTLS & 0x70) == 0x20 && POKEY_siocheck())
			SIO_PutByte(byte);
		DELAYED_SEROUT_IRQ = SEROUT_INTERVAL;
		IRQST |= 0x08;
		DELAYED_XMTDONE_IRQ = XMTDONE_INTERVAL;
#ifdef SERIO_SOUND
		Update_serio_sound(1,byte);
#endif
		break;
	case _STIMER:
		DivNIRQ[CHAN1] = DivNMax[CHAN1];
		DivNIRQ[CHAN2] = DivNMax[CHAN2];
		DivNIRQ[CHAN4] = DivNMax[CHAN4];
#ifdef DEBUG1
		printf("WR: STIMER = %x\n", byte);
#endif
		break;
	case _SKCTLS:
		SKCTLS = byte;
		if (byte & 4)
			pot_scanline = 228;	// fast pot mode - return results immediately
		break;
*/
#ifdef STEREO
#ifdef __PLUS
	if( stereo_enabled )
	{
#endif /*__PLUS*/
	case _AUDC1 + _POKEY2:
		AUDC[CHAN1 + CHIP2] = byte;
		Update_pokey_sound(_AUDC1, byte, 1, SOUND_GAIN);
		break;
	case _AUDC2 + _POKEY2:
		AUDC[CHAN2 + CHIP2] = byte;
		Update_pokey_sound(_AUDC2, byte, 1, SOUND_GAIN);
		break;
	case _AUDC3 + _POKEY2:
		AUDC[CHAN3 + CHIP2] = byte;
		Update_pokey_sound(_AUDC3, byte, 1, SOUND_GAIN);
		break;
	case _AUDC4 + _POKEY2:
		AUDC[CHAN4 + CHIP2] = byte;
		Update_pokey_sound(_AUDC4, byte, 1, SOUND_GAIN);
		break;
	case _AUDCTL + _POKEY2:
		AUDCTL[1] = byte;
		/* determine the base multiplier for the 'div by n' calculations */
		if (byte & CLOCK_15)
			Base_mult[1] = DIV_15;
		else
			Base_mult[1] = DIV_64;

		Update_pokey_sound(_AUDCTL, byte, 1, SOUND_GAIN);
		break;
	case _AUDF1 + _POKEY2:
		AUDF[CHAN1 + CHIP2] = byte;
		Update_pokey_sound(_AUDF1, byte, 1, SOUND_GAIN);
		break;
	case _AUDF2 + _POKEY2:
		AUDF[CHAN2 + CHIP2] = byte;
		Update_pokey_sound(_AUDF2, byte, 1, SOUND_GAIN);
		break;
	case _AUDF3 + _POKEY2:
		AUDF[CHAN3 + CHIP2] = byte;
		Update_pokey_sound(_AUDF3, byte, 1, SOUND_GAIN);
		break;
	case _AUDF4 + _POKEY2:
		AUDF[CHAN4 + CHIP2] = byte;
		Update_pokey_sound(_AUDF4, byte, 1, SOUND_GAIN);
		break;
#ifdef __PLUS
	}
#endif /*__PLUS*/
#endif
	}
}

FILE *fout = 0;
#define PORT 1
#define SPEED 115200
//#define SPEED 2000000
int port = PORT;
int speed = SPEED;
void Pokey_Initialise(int *argc, char *argv[])
{
	int i;
	int j;
	ULONG reg;

	if (OpenComport( port, speed))
	{
		printf( "couldn't open com port %d speed %d\n", port, speed);
		exit( 1);
	}
	fout = fopen("sa_pokey.out","wt");
	fprintf( fout, "%s: argc=%d\n", __func__, argc);
	/*
	 * Initialise Serial Port Interrupts
	 */

	DELAYED_SERIN_IRQ = 0;
	DELAYED_SEROUT_IRQ = 0;
	DELAYED_XMTDONE_IRQ = 0;

	KBCODE = 0xff;
	SERIN = 0x00;	/* or 0xff ? */
	IRQST = 0xff;
	IRQEN = 0x00;
	SKSTAT = 0xff;
	SKCTLS = 0x00;

	for (i = 0; i < (MAXPOKEYS * 4); i++) {
		AUDC[i] = 0;
		AUDF[i] = 0;
	}

	for (i = 0; i < MAXPOKEYS; i++) {
		AUDCTL[i] = 0;
		Base_mult[i] = DIV_64;
	}

	for (i = 0; i < 4; i++)
		DivNIRQ[i] = DivNMax[i] = 0;

	pot_scanline = 0;

	/* initialise poly9_lookup */
	reg = 0x1ff;
	for (i = 0; i < 511; i++) {
		poly9_lookup[i] = (unsigned char)( reg >> 1);		//!
		reg |= (((reg >> 5) ^ reg) & 1) << 9;
		reg >>= 1;
	}
	/* initialise poly17_lookup */
	reg = 0x1ffff;
	for (i = 0; i < 16385; i++) {
		poly17_lookup[i] = (unsigned char)(reg >> 9 );		//!
		for (j = 0; j < 8; j++) {
			reg |= (((reg >> 5) ^ reg) & 1) << 17;
			reg >>= 1;
		}
	}

	random_scanline_counter = time(NULL) % POLY17_SIZE;
}

void POKEY_Frame(void)
{
	random_scanline_counter %= AUDCTL[0] & POLY9 ? POLY9_SIZE : POLY17_SIZE;
}

/***************************************************************************
 ** Generate POKEY Timer IRQs if required                                 **
 ** called on a per-scanline basis, not very precise, but good enough     **
 ** for most applications                                                 **
 ***************************************************************************/

/*!
void POKEY_Scanline(void)
{
#ifdef POKEY_UPDATE
	pokey_update();
#endif

#ifndef NO_VOL_ONLY
#ifdef __PLUS
	if( g_Sound.nDigitized )
#endif //__PLUS/
	Update_vol_only_sound();
#endif  // NO_VOL_ONLY 

	INPUT_Scanline();	// Handle Amiga and ST mice. 
						// It's not a part of POKEY emulation, 
						// but it looks to be the best place to put it. 

	if (pot_scanline < 228)
		pot_scanline++;

	random_scanline_counter += LINE_C;

	if (DELAYED_SERIN_IRQ > 0) {
		if (--DELAYED_SERIN_IRQ == 0) {
			if (IRQEN & 0x20) {
#ifdef DEBUG2
				printf("SERIO: SERIN Interrupt triggered\n");
#endif
				if (IRQST & 0x20) {
					IRQST &= 0xdf;
					SERIN = SIO_GetByte();
				}
				else
					SKSTAT &= 0xdf;
				GenerateIRQ();
			}
#ifdef DEBUG2
			else {
				printf("SERIO: SERIN Interrupt missed\n");
			}
#endif
		}
	}

	if (DELAYED_SEROUT_IRQ > 0) {
		if (--DELAYED_SEROUT_IRQ == 0) {
			if (IRQEN & 0x10) {
#ifdef DEBUG2
				printf("SERIO: SEROUT Interrupt triggered\n");
#endif
				IRQST &= 0xef;
				GenerateIRQ();
			}
#ifdef DEBUG2
			else {
				printf("SERIO: SEROUT Interrupt missed\n");
			}
#endif
		}
	}

	if (DELAYED_XMTDONE_IRQ > 0)
		if (--DELAYED_XMTDONE_IRQ == 0) {
			IRQST &= 0xf7;
			if (IRQEN & 0x08) {
#ifdef DEBUG2
				printf("SERIO: XMTDONE Interrupt triggered\n");
#endif
				GenerateIRQ();
			}
#ifdef DEBUG2
			else
				printf("SERIO: XMTDONE Interrupt missed\n");
#endif
		}

	if ((DivNIRQ[CHAN1] -= LINE_C) < 0 ) {
		DivNIRQ[CHAN1] += DivNMax[CHAN1];
		if (IRQEN & 0x01) {
			IRQST &= 0xfe;
			GenerateIRQ();
		}
	}

	if ((DivNIRQ[CHAN2] -= LINE_C) < 0 ) {
		DivNIRQ[CHAN2] += DivNMax[CHAN2];
		if (IRQEN & 0x02) {
			IRQST &= 0xfd;
			GenerateIRQ();
		}
	}

	if ((DivNIRQ[CHAN4] -= LINE_C) < 0 ) {
		DivNIRQ[CHAN4] += DivNMax[CHAN4];
		if (IRQEN & 0x04) {
			IRQST &= 0xfb;
			GenerateIRQ();
		}
	}
}
*/

/*****************************************************************************/
/* Module:  Update_Counter()                                                 */
/* Purpose: To process the latest control values stored in the AUDF, AUDC,   */
/*          and AUDCTL registers.  It pre-calculates as much information as  */
/*          possible for better performance.  This routine has been added    */
/*          here again as I need the precise frequency for the pokey timers  */
/*          again. The pokey emulation is therefore somewhat sub-optimal     */
/*          since the actual pokey emulation should grab the frequency values */
/*          directly from here instead of calculating them again.            */
/*                                                                           */
/* Author:  Ron Fries,Thomas Richter                                         */
/* Date:    March 27, 1998                                                   */
/*                                                                           */
/* Inputs:  chan_mask: Channel mask, one bit per channel.                    */
/*          The channels that need to be updated                             */
/*                                                                           */
/* Outputs: Adjusts local globals - no return value                          */
/*                                                                           */
/*****************************************************************************/

void Update_Counter(int chan_mask)
{

/************************************************************/
/* As defined in the manual, the exact Div_n_cnt values are */
/* different depending on the frequency and resolution:     */
/*    64 kHz or 15 kHz - AUDF + 1                           */
/*    1 MHz, 8-bit -     AUDF + 4                           */
/*    1 MHz, 16-bit -    AUDF[CHAN1]+256*AUDF[CHAN2] + 7    */
/************************************************************/

	/* only reset the channels that have changed */

	if (chan_mask & (1 << CHAN1)) {
		/* process channel 1 frequency */
		if (AUDCTL[0] & CH1_179)
			DivNMax[CHAN1] = AUDF[CHAN1] + 4;
		else
			DivNMax[CHAN1] = (AUDF[CHAN1] + 1) * Base_mult[0];
		if (DivNMax[CHAN1] < LINE_C)
			DivNMax[CHAN1] = LINE_C;
	}

	if (chan_mask & (1 << CHAN2)) {
		/* process channel 2 frequency */
		if (AUDCTL[0] & CH1_CH2) {
			if (AUDCTL[0] & CH1_179)
				DivNMax[CHAN2] = AUDF[CHAN2] * 256 + AUDF[CHAN1] + 7;
			else
				DivNMax[CHAN2] = (AUDF[CHAN2] * 256 + AUDF[CHAN1] + 1) * Base_mult[0];
		}
		else
			DivNMax[CHAN2] = (AUDF[CHAN2] + 1) * Base_mult[0];
		if (DivNMax[CHAN2] < LINE_C)
			DivNMax[CHAN2] = LINE_C;
	}

	if (chan_mask & (1 << CHAN4)) {
		/* process channel 4 frequency */
		if (AUDCTL[0] & CH3_CH4) {
			if (AUDCTL[0] & CH3_179)
				DivNMax[CHAN4] = AUDF[CHAN4] * 256 + AUDF[CHAN3] + 7;
			else
				DivNMax[CHAN4] = (AUDF[CHAN4] * 256 + AUDF[CHAN3] + 1) * Base_mult[0];
		}
		else
			DivNMax[CHAN4] = (AUDF[CHAN4] + 1) * Base_mult[0];
		if (DivNMax[CHAN4] < LINE_C)
			DivNMax[CHAN4] = LINE_C;
	}
}

/*!
void POKEYStateSave( void )
{
	int SHIFT_KEY = 0;
	int KEYPRESSED = 0;

	SaveUBYTE( &KBCODE, 1 );
	SaveUBYTE( &IRQST, 1 );
	SaveUBYTE( &IRQEN, 1 );
	SaveUBYTE( &SKCTLS, 1 );

	SaveINT( &SHIFT_KEY, 1 );
	SaveINT( &KEYPRESSED, 1 );
	SaveINT( &DELAYED_SERIN_IRQ, 1 );
	SaveINT( &DELAYED_SEROUT_IRQ, 1 );
	SaveINT( &DELAYED_XMTDONE_IRQ, 1 );

	SaveUBYTE( &AUDF[0], 4 );
	SaveUBYTE( &AUDC[0], 4 );
	SaveUBYTE( &AUDCTL[0], 1 );

	SaveINT((int *)&DivNIRQ[0], 4);
	SaveINT((int *)&DivNMax[0], 4);
	SaveINT((int *)&Base_mult[0], 1 );
}

void POKEYStateRead( void )
{
        int i;
	int SHIFT_KEY;
	int KEYPRESSED;

	ReadUBYTE( &KBCODE, 1 );
	ReadUBYTE( &IRQST, 1 );
	ReadUBYTE( &IRQEN, 1 );
	ReadUBYTE( &SKCTLS, 1 );

	ReadINT( &SHIFT_KEY, 1 );
	ReadINT( &KEYPRESSED, 1 );
	ReadINT( &DELAYED_SERIN_IRQ, 1 );
	ReadINT( &DELAYED_SEROUT_IRQ, 1 );
	ReadINT( &DELAYED_XMTDONE_IRQ, 1 );

	ReadUBYTE( &AUDF[0], 4 );
	ReadUBYTE( &AUDC[0], 4 );
	ReadUBYTE( &AUDCTL[0], 1 );
        for (i = 0; i < 4; i++)
        {
                POKEY_PutByte(_AUDF1 + i * 2, AUDF[i]);
                POKEY_PutByte(_AUDC1 + i * 2, AUDC[i]);
        }
        POKEY_PutByte(_AUDCTL, AUDCTL[0]);

	ReadINT((int *)&DivNIRQ[0], 4);
	ReadINT((int *)&DivNMax[0], 4);
	ReadINT((int *)&Base_mult[0], 1 );
}
*/
