package com.kierek.chip8emu.emu;

import com.badlogic.gdx.Gdx;
import com.kierek.chip8emu.Chip8Emu;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Processor {

    private static final String TAG = "Processor";

    private static final int fontset[] = new int[]{
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    //needed for CXNN opcode;
    private Random ran;

    private int[] memory;
    private int[] register;

    //needed for performing jumps to subroutines
    private LinkedList<Integer> stack;

    //programCounter points to memory location with next opcode
    private int programCounter;
    private int addressPointer;

    private int delayTimer, soundTimer;

    private Chip8Emu emu;

    private float runTime;

    public Processor(Chip8Emu emu) {
        this.emu = emu;
        ran = new Random();

        //These machines had 4096 (0x1000) memory locations
        memory = new int[0x1000];
        //register named from V0 to VF
        register = new int[16];

        //in 0x000-0x1FF was an interpreter, so we can store font data here
        System.arraycopy(fontset, 0, memory, 0, fontset.length);
    }

    public void emulateCycle(float deltaTime) {
        //hacky solution for controlling emulation speed
        while (runTime < .1f) {
            decodeOpCode(getNextOpCode());

            //need to do something about speed and timers
            runTime += deltaTime;
        }

        if (delayTimer > 0) delayTimer--;
        if (soundTimer > 0) {
            soundTimer--;
        }

        runTime = 0;
    }

    private int getNextOpCode() {
        //since every opcode is 2 bytes long, we need to fetch them
        //and then merge into one

        int op1 = memory[programCounter];
        int op2 = memory[programCounter + 1];

        programCounter += 2;

        return ((op1 << 8) | op2);
    }

    private void decodeOpCode(int opcode) {
        switch (opcode) {
            //Clears the screen.
            case 0x00E0:
                emu.getRenderer().clearDisplay();
                return;
            //Returns from a subroutine.
            case 0x00EE:
                programCounter = stack.pop();
                return;
        }

        switch (opcode & 0xF000) {
            //1NNN = goto NNN;
            case 0x1000:
                programCounter = opcode & 0xFFF;
                return;
            //2NNN = call subroutine at NNN;
            case 0x2000:
                //store current position in stack to return later
                stack.push(programCounter);

                //go to subroutine address
                programCounter = opcode & 0xFFF;
                return;
            //3XNN = Skips the next instruction if register[X] equals NN.
            case 0x3000:
                if (register[(opcode & 0xF00) >>> 8] == (opcode & 0xFF))
                    programCounter += 2;
                return;
            //4XNN = Skips he next instruction if register[X] doesn't equal NN.
            case 0x4000:
                if (register[(opcode & 0xF00) >>> 8] != (opcode & 0xFF)) {
                    programCounter += 2;
                }
                return;
            //6XNN = Sets register[X] to NN.
            case 0x6000:
                register[(opcode & 0xF00) >>> 8] = opcode & 0xFF;
                return;
            //7XNN = Adds NN to register[X].
            //remember that we operate on bytes, and since I use int,
            //I need to do bitwise and with 0xFF,
            //to be sure that the value won't be greater than 255
            case 0x7000:
                register[(opcode & 0xF00) >>> 8] += (opcode & 0xFF);
                register[(opcode & 0xF00) >>> 8] &= 0xFF;
                return;
            //ANNN = Sets I to the address NNN.
            case 0xA000:
                addressPointer = (opcode & 0xFFF);
                return;
            //BNNN = Jumps to the address NNN plus V0.
            case 0xB000:
                programCounter = (opcode & 0xFFF) + register[0];
                return;
            //CXNN = Sets register[X] to the result of a bitwise and operation on a random number (Typically: 0 to 255) and NN.
            case 0xC000:
                register[(opcode & 0xF00) >>> 8] = ((ran.nextInt(256)) & (opcode & 0xFF));
                return;
            //DXYN
            //Draws a sprite at coordinate (VX, VY) that has a width of 8 pixels and a height of N pixels.
            //Each row of 8 pixels is read as bit-coded starting from memory location I;
            //I value doesn’t change after the execution of this instruction.
            //As described above, VF is set to 1 if any screen pixels are flipped from set to unset when the sprite is drawn, and to 0 if that doesn’t happen
            case 0xD000:
                register[0xF] = 0;

                //fetch position and height of the sprite
                int x = register[(opcode & 0xF00) >>> 8];
                int y = register[(opcode & 0xF0) >>> 4];
                int height = opcode & 0xF;

                int[] sprite = new int[height];

                System.arraycopy(memory, addressPointer, sprite, 0, height);

                register[0xF] |= emu.getRenderer().drawSprite(x, y, sprite) ? 1 : 0;
                return;
        }

        switch (opcode & 0xF00F) {
            //5XY0 = Skips the next instruction if VX equals VY.
            case 0x5000:
                if (register[(opcode & 0xF00) >>> 8] == register[(opcode & 0xF0) >>> 4])
                    programCounter += 2;
                return;
            //8XY0 = Sets VX to the value of VY.
            case 0x8000:
                register[(opcode & 0xF00) >>> 8] = register[(opcode & 0xF0) >>> 4];
                return;
            //8XY1 = Sets VX to VX or VY. (Bitwise OR operation)
            case 0x8001:
                register[(opcode & 0xF00) >>> 8] = register[(opcode & 0xF0) >>> 4];
                return;
            //8XY2 = Sets VX to VX and VY. (Bitwise AND operation)
            case 0x8002:
                register[(opcode & 0xF00) >>> 8] &= register[(opcode & 0xF0) >>> 4];
                return;
            //8XY3 = Sets VX to VX xor VY.
            case 0x8003:
                register[(opcode & 0xF00) >>> 8] ^= register[(opcode & 0xF0) >>> 4];
                return;
            //8XY4 = Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
            case 0x8004:
                int sum = register[(opcode & 0xF00) >>> 8] + register[(opcode & 0xF0) >>> 4];

                //check if was carry, meaning that sum is bigger than 0xFF
                register[0xF] = sum > 0xFF ? 1 : 0;
                register[(opcode & 0xF00) >>> 8] = sum;

                //remember that these are all bytes!
                register[(opcode & 0xF00) >>> 8] &= 0xFF;
                return;
            //8XY5 = VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
            case 0x8005:
                register[0xF] = 1;

                int regX = ((opcode & 0xF00) >>> 8);
                int regY = ((opcode & 0xF0) >>> 4);

                if (register[regY] > register[regX]) register[0xF] = 0;

                register[regX] -= register[regY];
                register[regX] &= 0xFF;
                return;
            //8XY6 = Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.[2]
            case 0x8006:
                register[0xF] = ((register[(opcode & 0xF00) >>> 8]) & 1) == 1 ? 1 : 0;
                register[(opcode & 0xF00) >>> 8] = register[(opcode & 0xF00) >>> 8] >>> 1;
                return;
            //8XY7 = Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
            case 0x8007:
                register[0xF] = 1;

                regX = ((opcode & 0xF00) >>> 8);
                regY = ((opcode & 0xF0) >>> 4);

                if (register[regX] > register[regY]) register[0xF] = 0;

                register[regX] = register[regY] = register[regX];
                register[regX] &= 0xFF;
                return;
            //8XYE = Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.[2]
            case 0x800E:
                int mostSignificantBit = (register[(opcode & 0xF00) >>> 8] >>> 7);
                register[0xF] = mostSignificantBit;
                register[(opcode & 0xF00) >>> 8] <<= 1;
                register[(opcode & 0xF00) >>> 8] &= 0xFF;
                return;
            //9XY0 = Skips the next instruction if VX doesn't equal VY.
            case 0x9000:
                if (register[(opcode & 0xF00) >>> 8] != register[(opcode & 0xF0) >>> 4]) {
                    programCounter += 2;
                }
                return;
        }

        switch (opcode & 0xF0FF) {
            //EX9E = Skips the next instruction if the key stored in VX is pressed.
            case 0xE09E:
                if (emu.getInput().getKeyStatus(register[(opcode & 0xF00) >>> 8])) {
                    programCounter += 2;
                }
                return;
            //EXA1 = Skips the next instruction if the key stored in VX isn't pressed.
            case 0xE0A1:
                if (!emu.getInput().getKeyStatus(register[(opcode & 0xF00) >>> 8])) {
                    programCounter += 2;
                }
                return;
            //FX07 	= Sets VX to the value of the delay timer.
            case 0xF007:
                register[(opcode & 0xF00) >>> 8] = (delayTimer & 0xFF);
                return;
            //FX0A = A key press is awaited, and then stored in VX. (Blocking Operation. All instruction halted until next key event)
            case 0xF00A:
                Gdx.app.log(TAG, "notImplemented: " + Integer.toHexString(opcode));
                return;
            //FX15 = Sets the delay timer to VX.
            case 0xF015:
                delayTimer = register[(opcode & 0xF00) >>> 8];
                return;
            //FX18 = Sets the sound timer to VX.
            case 0xF018:
                soundTimer = (opcode & 0xF00) >>> 8;
                return;
            //FX1E = Adds VX to I.
            case 0xF01E:
                addressPointer += register[(opcode & 0xF00) >>> 8];
                addressPointer &= 0xFFF;
                return;
            //FX29 = Sets I to the location of the sprite for the character in VX. Characters 0-F (in hexadecimal) are represented by a 4x5 font.
            case 0xF029:
                addressPointer = register[(opcode & 0xF00) >>> 8] * 5;
                return;
            //FX33 = Stores the binary-coded decimal representation of VX, with the most significant of three digits at the address in I,
            //the middle digit at I plus 1, and the least significant digit at I plus 2.
            //(In other words, take the decimal representation of VX, place the hundreds digit in memory at location in I,
            //the tens digit at location I+1, and the ones digit at location I+2.)
            case 0xF033:
                int decVX = register[(opcode & 0xF00) >>> 8];
                int hundreds = decVX / 100;
                int tens = (decVX / 10) % 10;
                int units = decVX % 10;

                memory[addressPointer] = hundreds;
                memory[addressPointer + 1] = tens;
                memory[addressPointer + 2] = units;
                return;
            //FX55 = Stores V0 to VX (including VX) in memory starting at address I.
            case 0xF055:
                System.arraycopy(register, 0, memory, addressPointer, ((opcode & 0xF00) >>> 8) + 1);
                return;
            //FX65 = Fills V0 to VX (including VX) with values from memory starting at address I.
            case 0xF065:
                System.arraycopy(memory, addressPointer, register, 0, ((opcode & 0xF00) >>> 8) + 1);
        }
    }

    public void loadROM(String selectedFilePath) {
        initialize();

        //loading ROM into memory at 0x200 and onwards

        byte[] romAsBytes = Gdx.files.internal(selectedFilePath).readBytes();
        for (int i = 0; i < romAsBytes.length; i++) {
            //& 0xFF because we want unsigned byte for easier debugging
            memory[i + 0x200] = romAsBytes[i] & 0xFF;
        }
    }

    private void initialize() {
        //clear memory, register V0-VF and stack
        Arrays.fill(memory, 0x200, memory.length, 0);
        Arrays.fill(register, 0);

        if (stack == null)
            stack = new LinkedList<Integer>();
        else
            stack.clear();

        //clear display;
        emu.getRenderer().clearDisplay();

        //reset program counter, index register, stack pointer, timers
        programCounter = 0x200;
        addressPointer = 0;
        delayTimer = 0;
        soundTimer = 0;
    }
}
