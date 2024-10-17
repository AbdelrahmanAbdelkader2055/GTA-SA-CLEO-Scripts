SCRIPT_START
{
    NOP
    //Variables declaration
    LVAR_INT checkpoint_type checkpoint_id checkpoint_id_old new_checkpoint_sound checkpoint_sound_first_byte

    LVAR_FLOAT checkpoint_coords[6] audio_vol

    LOAD_3D_AUDIO_STREAM "cleo\cleo_sounds\MCLACheck.mp3" new_checkpoint_sound


    LVAR_INT scplayer smoke_effect

    GET_PLAYER_CHAR 0 scplayer

    
    WHILE TRUE
        WAIT 0

     
        //read checkpoint type. 0-2 race checkpoints, 3-4 plane checkpoints, 5-8 others
        READ_MEMORY 0xC7F158 1 0 checkpoint_type
        
        //read current checkpoint identifier value
        READ_MEMORY 0xC7F15C 4 0 checkpoint_id

        //store old coordinates
        checkpoint_coords[3] = checkpoint_coords[0]
        checkpoint_coords[4] = checkpoint_coords[1]
        checkpoint_coords[5] = checkpoint_coords[2]

        //read current checkpoint coordinates x, y, z and store them to the vector
        READ_MEMORY 0xC7F168 4 0 checkpoint_coords[0]
        READ_MEMORY 0xC7F16C 4 0 checkpoint_coords[1]
        READ_MEMORY 0xC7F170 4 0 checkpoint_coords[2]


        IF checkpoint_type <= 2

        AND NOT IS_CHAR_IN_ANY_PLANE scplayer
        AND NOT IS_CHAR_IN_ANY_HELI scplayer
           
            //disable global checkpoint renderer if street race
            MAKE_NOP 0x72606F 5

            IF checkpoint_id = 0
                GOSUB destroy_fx_smoke
            ENDIF

            IF NOT checkpoint_id = checkpoint_id_old
            AND NOT checkpoint_id = 0
                //disable original checkpoints sound
                MAKE_NOP 0x4EE5ED 19
                checkpoint_id_old = checkpoint_id
                GOSUB destroy_fx_smoke
                GOSUB create_fx_smoke
                GOSUB play_new_checkpoint_sound
            ENDIF
        ELSE
            GOSUB enable_checkpoints_renderer
        ENDIF

        READ_MEMORY 0x4EE5ED 1 0 checkpoint_sound_first_byte

        IF checkpoint_id = 0
        AND checkpoint_sound_first_byte = 0x90
            GOSUB enable_original_checkpoint_sound
        ENDIF 
    ENDWHILE

    

    play_new_checkpoint_sound:
        GET_AUDIO_SFX_VOLUME audio_vol
        SET_AUDIO_STREAM_VOLUME new_checkpoint_sound audio_vol
        SET_PLAY_3D_AUDIO_STREAM_AT_COORDS new_checkpoint_sound (checkpoint_coords[3], checkpoint_coords[4], checkpoint_coords[5])
        SET_AUDIO_STREAM_STATE new_checkpoint_sound 1
    RETURN

    create_fx_smoke:
        CREATE_FX_SYSTEM "smoke_flare" (checkpoint_coords[0], checkpoint_coords[1], checkpoint_coords[2]) 1 smoke_effect
        PLAY_FX_SYSTEM smoke_effect
    RETURN

    destroy_fx_smoke:
        IF NOT smoke_effect = 0
            STOP_FX_SYSTEM smoke_effect
            KILL_FX_SYSTEM smoke_effect
        ENDIF
    RETURN

    //special thanks for vladvo for helping me out on this and the disable checkpoint renderer memory address
    enable_checkpoints_renderer:
        WRITE_MEMORY 0x72606F 4 0xE8 0
        WRITE_MEMORY 0x726070 1 0x8C 0
        WRITE_MEMORY 0x726071 1 0xFB 0
        WRITE_MEMORY 0x726072 1 0xFF 0
        WRITE_MEMORY 0x726073 1 0xFF 0
    RETURN

    enable_original_checkpoint_sound:

        WRITE_MEMORY 0x4EE5ED 1 0x68 0    // Opcode for pushing a 32-bit immediate value
        WRITE_MEMORY 0x4EE5EE 4 0x3F800000 0 

        WRITE_MEMORY 0x4EE5F2 1 0x6A 0   
        WRITE_MEMORY 0x4EE5F3 1 0 0  

        WRITE_MEMORY 0x4EE5F4 1 0x6A 0  
        WRITE_MEMORY 0x4EE5F5 1 0x2B 0 

        WRITE_MEMORY 0x4EE5F6 1 0xB9 0       // Opcode for MOV ECX, imm32
        WRITE_MEMORY 0x4EE5F7 4 0x00B6BC90 0    
       

        WRITE_MEMORY 0x4EE5FB 1 0xE8 0       // Opcode for CALL rel32
        WRITE_MEMORY 0x4EE5FC 4 0x000188A0    0   
        


    RETURN

}
SCRIPT_END
