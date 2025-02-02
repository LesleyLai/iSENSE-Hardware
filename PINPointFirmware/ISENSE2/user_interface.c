/**
 * @Copyright (c) 2008, iSENSE Project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the University of
 * Massachusetts Lowell nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

/**
 * @file user_interface.c
 * @author Michael McGuinness <mmcguinn@cs.uml.edu>
 *
 * Interface functions for the user interface.
 */

#include "data_interface.h"
#include "user_interface.h"
#include "timer_interface.h"

/**
 * Initializes the button and LED port settings.
 */
void user_Init(void)
{
    //LEDs
    setPinMode(MODE_LOW, LED_POWER_PORT, LED_POWER_PIN);
    setPinMode(MODE_LOW, LED_GPS_PORT, LED_GPS_PIN);
    setPinMode(MODE_LOW, LED_1_PORT, LED_1_PIN);
    setPinMode(MODE_LOW, LED_2_PORT, LED_2_PIN);
    setPinMode(MODE_LOW, LED_3_PORT, LED_3_PIN);

    //Buttons
    setPinMode(MODE_INPUT_SRC, BUTTON_PORT, BUTTON_1_PIN);
    setPinMode(MODE_INPUT_SRC, BUTTON_PORT, BUTTON_2_PIN);
    setPinMode(MODE_INPUT_SRC, BUTTON_PORT, BUTTON_3_PIN);
}

/**
 * Returns the current state of the button 'num'.
 *
 * @param num Button to test.
 *
 * @return Returns if the button is pressed.
 */
char user_Get_Button(char num)
{
    switch (num)
    {
        case 1:
            return !(test_bit(BUTTON_IN, BUTTON_1_PIN));
            break;
        case 2:
            return !(test_bit(BUTTON_IN, BUTTON_2_PIN));
            break;
        case 3:
            return !(test_bit(BUTTON_IN, BUTTON_3_PIN));
            break;
    }

    return 0;
}


/**
 * Sets the given LED to the given state.
 *
 * @param num LED to change.
 * @param state State to set.
 */
void user_Set_LED(char num, char state)
{
    switch (num)
    {
        case LED_CLEAR:
            if (!state) setPinMode(MODE_HIGH, LED_1_PORT, LED_1_PIN);
            else setPinMode(MODE_LOW, LED_1_PORT, LED_1_PIN);
            break;
        case LED_STOP:
            if (!state) setPinMode(MODE_HIGH, LED_2_PORT, LED_2_PIN);
            else setPinMode(MODE_LOW, LED_2_PORT, LED_2_PIN);
            break;
        case LED_START:
            if (!state) setPinMode(MODE_HIGH, LED_3_PORT, LED_3_PIN);
            else setPinMode(MODE_LOW, LED_3_PORT, LED_3_PIN);
            break;
        case LED_POWER:
            if (!state) setPinMode(MODE_HIGH, LED_POWER_PORT, LED_POWER_PIN);
            else setPinMode(MODE_LOW, LED_POWER_PORT, LED_POWER_PIN);
            break;
        case LED_GPS:
            if (!state) setPinMode(MODE_HIGH, LED_GPS_PORT, LED_GPS_PIN);
            else setPinMode(MODE_LOW, LED_GPS_PORT, LED_GPS_PIN);
            break;

    }
}

/**
 * Toggles the given LED.
 *
 * @param num LED to toggle.
 */
void user_Toggle_LED(char num)
{
    switch (num)
    {
        case LED_CLEAR:
            togglePinOutput(LED_1_PORT, LED_1_PIN);
            break;
        case LED_STOP:
            togglePinOutput(LED_2_PORT, LED_2_PIN);
            break;
        case LED_START:
            togglePinOutput(LED_3_PORT, LED_3_PIN);
            break;
        case LED_POWER:
            togglePinOutput(LED_POWER_PORT, LED_POWER_PIN);
            break;
        case LED_GPS:
            togglePinOutput(LED_GPS_PORT, LED_GPS_PIN);
            break;
    }
}

/**
 * Handles all actions related to button presses during operation,
 * including LED changes. Any change to runData to relayed through
 * the given pointer, such as the recording state. Also handles the
 * clear button implementation.
 *
 * @param runData Address of the runData structure.
 */
void user_Handle_Buttons(RunData *runData)
{
    static unsigned int buttonTimer[3] = {0, 0, 0};
    static unsigned int buttonLastTime[3] = {0, 0, 0};
    int i;

    if (user_Get_Button(BUTTON_CLEAR))
    {
        user_Set_LED(LED_CLEAR, ON);
        runData->liveData = false;

        if (timer_Job_Ready4(&buttonTimer[BUTTON_CLEAR - 1], 3000, &buttonLastTime[BUTTON_CLEAR - 1], false))
        {
            runData->record = false;
            user_Set_LED(LED_STOP, ON);
            user_Set_LED(LED_START, OFF);
            data_Clear();

            for (i = 0; i < 50; i++)
            {
                timer_Wait_MS(50);
                user_Toggle_LED(LED_CLEAR);
            }
        }
    }
    else
    {
        if ((runData->record || runData->liveData) && 
            (runData->recordTimer > runData->recordPeriod - 5))
        {
            user_Set_LED(BUTTON_CLEAR, ON);
        }
        else
        {
            user_Set_LED(BUTTON_CLEAR, OFF);
        }
        buttonTimer[BUTTON_CLEAR - 1] = 0;
    }

    if (user_Get_Button(BUTTON_START))
    {
        runData->record = true;
        runData->liveData = false;
    }

    if (user_Get_Button(BUTTON_STOP))
    {
        runData->record = false;
        runData->liveData = false;
    }

    if (runData->record || runData->liveData)
    {
        user_Set_LED(LED_START, ON);
        user_Set_LED(LED_STOP, OFF);
    }
    else
    {
        user_Set_LED(LED_STOP, ON);
        user_Set_LED(LED_START, OFF);
    }
}
