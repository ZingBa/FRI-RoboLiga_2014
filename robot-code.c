#pragma config(Sensor, S3,     lightSensor,    sensorLightInactive)
#pragma config(Motor,  motorA,          rightMotor,    tmotorNormal, PIDControl, encoder)
#pragma config(Motor,  motorB,          handMotor,     tmotorNormal, PIDControl, encoder)
#pragma config(Motor,  motorC,          leftMotor,     tmotorNormal, PIDControl, encoder)
//#pragma config(Sensor, S4,     sonarSensor,    sensorSONAR)

//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//

//              LEFT MOTOR: C                 RIGHT MOTOR: A					HAND MOTOR: B

//*!!Code automatically generated by 'ROBOTC' configuration wizard               !!*//


//---------------------------------------------------------------start:initialization for BLUETOOTH
#define communication
#define CHANNEL 0 		 	//communication channel
#define BUFFER_SIZE 51 		//data package size +0
#define MAILBOX mailbox1 	//mailbox number
//---------------------------------------------------------------end:initialization for BLUETOOTH


//---------------------------------------------------------------start:initialization of FUNCTIONS
void straightByTime(int heading, int pwr, int time);
void straightByDistance(int heading, int distance);
//void turnByTime(int heading, int pwr, int time);
void turnByDegrees(int heading, int angle);
void holdPosition(int time);
void handToPosition(int position);
void grabItem();
void dropItem();
void blinkLight();
void celebrate();
ubyte receiveMsg(ubyte *msg, ubyte maxLength);
//ubyte sendMsg(ubyte *msg, ubyte length);
bool checkBTLinkConnected();
//---------------------------------------------------------------end:initialization of FUNCTIONS

//---------------------------------------------------------------start: ****** MAIN ******
//send and receive buffer arrays
ubyte recvBuf[BUFFER_SIZE];
//ubyte sendBuf[2]={1,2};

//GLOBAL VARIABLES
int handPosition = 2;

task main()
{
    //DEFAULT VALUES

    int d;		//dataPackageSize

    //wait for BT link to be established
    while(checkBTLinkConnected() != true)
    {        wait10Msec(10);      }

    //give a confirmation sign for BT connection
    if(checkBTLinkConnected() == true)
    {
      straightByTime(1, 50, 2);
      straightByTime(2, 40, 2);
      handToPosition(1);
      handPosition = 1;
    }

    //BT connection remains active throughout the game

    /*

        BEFORE WHILE VARIABLE DECLARATION
                    +
         SET DEFAULT VALUES

    */

    // variables used for motor management
    int heading; int pwr; int time;  //int dgre;
    // variables used for reading BUFFER :: COMMANDS
    char command[5]; char type; int amount; char num1[2]; char num2[3]; char num3[4];
        // variables used for indexing buffer
          int cdIndx = 0; int indx = 0; int commandLngth = 0;


    while(1)
    {

      d=receiveMsg(recvBuf,BUFFER_SIZE);  //get the size of data package

      if(d>0 && d <= 50)  //data package is of size
			{
			    //set default values
          heading = 1; pwr = 50; time = 20;  //dgre = 0;
      //  char command[5]; char type; int amount; char num1[2]; char num2[3]; char num3[4];
          cdIndx = 0;

			    indx = 0; commandLngth = 0;
		      //	char c;
					while(recvBuf[indx] != '*')
					{
					  while(recvBuf[indx] != '*' && recvBuf[indx] != '-')
					  {
					     //copy into command array
					     command[cdIndx] = recvBuf[indx];
					     commandLngth++;

					     indx++;
					     cdIndx++;

					  }
					  // ----- command TYPE start ---------------------------------------------
					  //in case only one letter command is received
					  if(commandLngth == 1){
					    type = command[0];
					  }
					  else // when command type is followed by a number
					  {
			        type = command[0];
			        // ----- command TYPE end -----------------------------------------------
				      // ----- command AMOUNT start -------------------------------------------
						        if(commandLngth == 4) //number is 3 digits long
						        {
						          for(int i = 1; i < commandLngth; i++)
						          {
						            num3[i-1] = command[i];
						            amount = atoi(num3);
						          }
						        }
						        else if(commandLngth == 3) //number is 2 digits long
						        {
						          for(int i = 1; i < commandLngth; i++)
						          {
						            num2[i-1] = command[i];
						            amount = atoi(num2);
						          }
						        }
						        else if(commandLngth == 2) //number is 1 digits long
						        {
						          for(int i = 1; i < commandLngth; i++)
						          {
						            num1[i-1] = command[i];
						            amount = atoi(num1);
						          }
						        }
					  }
					      // ----- command AMOUNT end ----------------------------------------------

							  // =====================   COMMAND  EXECUTION :  START   ===================== //
				            switch(type)
									  {
								    //  ----------------   MOVEMENT  ----------------   //
									    case 'M': straightByDistance(heading, amount);
									              break;
									    case 'F': straightByTime(1, pwr, time);
									              break;
									   // case 'S': straightByTime(heading, 100, time);
									    //          break;
									  //  case 'B': straightByTime(2, pwr, time);
									  //            break;
								    //  ----------------     TURN    ----------------   //
									    case 'L': turnByDegrees(1, amount);
									              break;
									    case 'R': turnByDegrees(2, amount);
									              break;
								    //  ----------------   SETTINGS  ----------------   //
									    case 'H': heading = amount;
									              break;
									    case 'P': pwr = amount;
									              break;
									    case 'T': time = amount;   //actual time ==> time * 100;
									              break;
									  //  ----------------    HOLD     ----------------   //
										case 'W': holdPosition(time);
									              break;
										//  ---------------- ARM MANIPULATION ----------------   //
											case 'U': handToPosition(1);
													  break;
											case 'D': dropItem();
													  break;
											case 'G': grabItem();
													  break;
											case 'S': handToPosition(2);
													  break;
								//  ----------------  SENSORS  ----------------   //
											case 'B': blinkLight();
													  break;
											case 'C': celebrate();
													  break;
			    			    //  ----------------   DEFAULT   ----------------   //
									    default:
									              PlaySoundFile("Woops.rso");
									              break;
									  }

					  // =====================   COMMAND  EXECUTION :   END    ===================== //
					  if(recvBuf[indx] == '-')
					  {
					    commandLngth = 0;
					    cdIndx = 0;
					    indx++;
					    /*
					    for(int i = 0; i <5; i++)
					    {
					      command[i] = '0';
					    }*/
					  }
					  else if(recvBuf[indx] == '*')
					  {
					    break;
					  }
					}
					//end of both while loops

					/*
					      FREE ALL PREVIOUSLY USED SPACE
					*/
					//free(heading); free(pwr); free(time); free(dgre); free(command); free(type); free(amount);
					//free(num1); free(num2); free(num3); free(cdIndx);

		  }
		  else if(d > 50)   // data package is too big
		  {
			   nxtDisplayTextLine(1,"ERROR");
			   nxtDisplayTextLine(2,"Size too big");
			   wait1Msec(500);
			   eraseDisplay();
		  }

		  //check if the connection still exists
		  if(checkBTLinkConnected() == false)
		  {
			handToPosition(2);
			handPosition = 2;
			break;
		  }


	  } // end of while(1)
}

//---------------------------------------------------------------end: ****** MAIN ******

/*
        MOVEMENT
*/

//---------------------------------------------------------------start:methodForMovement1.0  FORWARD
void straightByTime(int heading, int pwr, int time) //speed[],time[ms]
{
	//if(handPosition == 2)	handGrab(1);

  if(heading == 2)
  {
    pwr = -pwr;
  }

    time = time * 100;
    motor[motorA]= pwr;    motor[motorC]= pwr;
                wait1Msec (time);
    motor[motorA]= 0;        motor[motorC]= 0;
                wait1Msec(10);
}
//---------------------------------------------------------------end:methodForMovement1.0


//---------------------------------------------------------------start:methodForMovement2.0
void straightByDistance(int heading, int distance)
{ // speed[],distance[cm]

//   	SAFETY
  if(heading == 1 || heading == 2 && distance > 0)
  {
		//if(handPosition == 2)	handToPosition(1);
	//		FUNCTION
		float ticks = 0;
		int pwr = 75;   //410'... == 19,5 cm    // 1 cm == 21'

		float dstncFctr = distance / 50;


		float factor = 20;
			//for every 50 cm add 30 ticks
	    ticks = (distance * factor) + (dstncFctr * 30);

		if(heading == 2)
		{
		  pwr = -pwr;
		}

	  nMotorEncoder[motorA] = 0;    //reset the value of encoder A to zero
	  nMotorEncoder[motorC] = 0;    //reset the value of encoder C to zero

	  nMotorEncoderTarget[motorA] = ticks;   //set the encoder target
	  nMotorEncoderTarget[motorC] = ticks;    //set the encoder target


	  motor[motorA] = pwr;    //turn on motorB at 50% power
	  motor[motorC] = pwr;    //turn on motorB at 50% power

	  while(nMotorRunState[motorA] != runStateIdle || nMotorRunState[motorC] != runStateIdle)
	  { /*brake gently*/  }

		motor[motorA] = 0;    //Turn off motorA
		motor[motorC] = 0;    //Turn off motorC
  }
}
//---------------------------------------------------------------end:methodForMovement2.0

/*
		ROTATION  // TURNS
*/

//---------------------------------------------------------------start:methodForRotation1.0  LEFT  &&  RIGHT
/*
void turnByTime(int heading, int pwr, int time) //(heading == 1) => Left ; (heading == 2) => Right ,speed[],time[ms]
{

	//if(handPosition == 2)	handGrab(1);

  if(heading == 1)
	{
	  nSyncedMotors = synchAC;
	  nSyncedTurnRatio = -100;
	  motor[motorA] = pwr;
	wait1Msec(time);
  	motor[motorA] = 0;
	}
	else if(heading == 2)
	{
	  nSyncedMotors = synchCA;
	  nSyncedTurnRatio = -100;
	  motor[motorC] = pwr;
	wait1Msec(time);
  	motor[motorC] = 0;
	}
  	nSyncedMotors = synchNone;

}
*/
//---------------------------------------------------------------end:methodForRotation1.0


//---------------------------------------------------------------start:methodForRotation2.0
void turnByDegrees(int heading, int angle)  //(heading == 1) => Left ; (heading == 2) => Right ,degrees[']
{

//   SAFETY
  if(heading == 1 || heading == 2 && angle > 0)
    {
		//if(handPosition == 2)	handToPosition(1);
		//	FUNCTION
		int pwr = 50;

		float factor = 3.1; //90' == 282ticks	float ticks = 282;
		float ticks;

		float anglFctr = angle / 90;
	  //nSyncedMotors = synchAC;  //nSyncedTurnRatio = -100;

	  //ticks             //ticks = (int)(angle * factor);
	  ticks = (angle * factor) +(anglFctr * 3);

		 nMotorEncoder[motorA] = 0;//reset the value of encoder A to zero
		 nMotorEncoder[motorC] = 0;//reset the value of encoder C to zero

		 nMotorEncoderTarget[motorA] = ticks;//set the encoder target
		 nMotorEncoderTarget[motorC] = ticks;//set the encoder target

		 if(heading == 2)
		 {
		   pwr = -pwr;
		 }

			motor[motorA] = pwr;//turn on motorB at 50% power
			motor[motorC] = -pwr;//turn on motorB at 50% power

		 while(nMotorRunState[motorA] != runStateIdle || nMotorRunState[motorC] != runStateIdle)
		 { // slam the brakes gently
		 }

		  motor[motorA] = 0; //Turn off motorA
		  motor[motorC] = 0; //Turn off motorC
	}
}

//---------------------------------------------------------------end:methodForRotation2.0

/*
      STATIC ACTIONS  (hold..)
*/

//---------------------------------------------------------------start:methodHoldStill
void holdPosition(int time)
{
	if(time > 0)
	{
	//if(handPosition == 2)	handToPosition(1);

	time = 100 *time;
	nxtDisplayTextLine(1,"Wait %d ms", time);
	wait1Msec(time);
	PlaySoundFile("! Attention.rso");
  }
}
//---------------------------------------------------------------end:methodHoldStill


/*
        HAND OPERATIONS
*/

//---------------------------------------------------------------start:methodForHandPosition
void handToPosition(int position)    //position@ 1 --> "UP"  @ 2 --> "DOWN"
{
  //   SAFETY
  if(position == 1 || position == 2)
    {
	  // FUNCTION
	  int pwr = 10;    int ticks = 90;

	  if(position == 1 && handPosition == 1)
	  {
	      return;     //don't do anything
	  }
	  else if(position == 2 && handPosition == 2)
	  {
	      return;   //don't do anything
	  }

	  //lower the arm in order to grab the item
	  if(position == 2)
	  {
	      pwr = -pwr;
	  }


	  nMotorEncoder[motorB] = 0;//reset the value of encoder A to zero
	    nMotorEncoderTarget[motorB] = ticks;//set the encoder target
		    motor[motorB] = pwr;//turn on motorB at 50% power

		while(nMotorRunState[motorB] != runStateIdle)
		{ // slam the brakes gently
		}

		motor[motorB] = 0; //Turn off motorB

		if(position == 1)
		{
		    handPosition = 1;
		}
		else
		{
		    handPosition = 2;
		}
  }

}
//---------------------------------------------------------------end:methodForHandPosition

//---------------------------------------------------------------start:methodItemGrab

void grabItem()
{
    if(handPosition == 1) //in case the arm is lifted
    {
        //move backwards, lower the arm, move forward, lift the arm
        straightByDistance(2, 8);
        handToPosition(2);
        straightByDistance(1, 16);
        handToPosition(1);
    }
    else      //in case the arm is lowered, drive forward and lift the arm
    {
        straightByDistance(1,4);
        handToPosition(1);
    }
}

//---------------------------------------------------------------end:methodItemGrab

//---------------------------------------------------------------start:methodItemGrab

void dropItem()
{
    if(handPosition == 1)
     {
         handToPosition(2);
         straightByDistance(2, 10);
         handPosition = 2;
     }
}

//---------------------------------------------------------------end:methodItemGrab

void celebrate()
{
	turnByDegrees(1, 75);
	PlaySoundFile("! Startup.rso");
	turnByDegrees(2, 150);
	PlaySoundFile("! Startup.rso");
	turnByDegrees(1, 75);
}

/*
      SENSOR INTERACTION
*/

//---------------------------------------------------------------start:methodHoldStill
void blinkLight()
{
  int lightActive = 0;
  int scnds = 6;

  int cicle = 0;
  int ciclesRequired = scnds * 2;

	 while(cicle < ciclesRequired)
   {
      if(lightActive == 0)
      {
          SensorType[lightSensor] = sensorLightActive;
          lightActive = 1;
          wait1Msec(500);
          cicle++;
      }
      else
      {
          SensorType[lightSensor] = sensorLightInactive;
          lightActive = 0;
          wait1Msec(500);
          cicle++;
      }
   }
   if(lightActive == 1) SensorType[lightSensor] = sensorLightInactive;
}
//---------------------------------------------------------------end:methodHoldStill



/*
    BLUETOOTH OPERATIONS
*/

//---------------------------------------------------------------start:methodsForBluetooth ::	BLUETOOTH ::

//---------------------------------------------------------------start:methodForConnection
//Preveri ce je BT povezava OK
bool checkBTLinkConnected()
{
	if (nBTCurrentStreamIndex >= 0)	  return true;
	  else return false;
}
//---------------------------------------------------------------end:methodForConnection


//---------------------------------------------------------------start:methodForSending
/*
  Funkcija za posiljanje sporocil
  Vrne 1, ce je bilo sporocilo poslano, sicer pa nic
*/
/**
ubyte sendMsg(ubyte *msg, ubyte length)
{
	ubyte sendBuf[BUFFER_SIZE];
	if (length>=BUFFER_SIZE)		return 0;
	memcpy(sendBuf,msg,length);
	sendBuf[length]=0;
  TFileIOResult nBTCmdErrorStatus;
	if (checkBTLinkConnected()){
  	nBTCmdErrorStatus = cCmdMessageWriteToBluetooth(CHANNEL, sendBuf, length+1, MAILBOX);
  	if ((nBTCmdErrorStatus == ioRsltSuccess) || (nBTCmdErrorStatus == ioRsltCommPending))
  	{
  		while (nBluetoothCmdStatus == ioRsltCommPending)
			{
				wait1Msec(1);
			}
  		return 1;
  	}
	}
	else return 0;
}
**/
//---------------------------------------------------------------end:methodForSending

//---------------------------------------------------------------start:methodForReceiving
/*
Funkcija za sprejemanje sporocil vrne dolzino prejetega sporocila
Vrne 0 ce ni nobenega sporocila v cakalni vrsti
*/

ubyte receiveMsg(ubyte *msg, ubyte maxLength)
{
  TFileIOResult nBTCmdRdErrorStatus;
	int nSizeOfMessage;
	if (checkBTLinkConnected()){
		memset(msg,0,maxLength);

		nSizeOfMessage = cCmdMessageGetSize(MAILBOX);
		if (nSizeOfMessage <= 0)
		{
			return 0;
		}
	  else{
		  nBTCmdRdErrorStatus = cCmdMessageRead(msg, maxLength, MAILBOX);
		  if (nBTCmdRdErrorStatus == ioRsltSuccess)
		  {
		  	return nSizeOfMessage;

			}
		  else
		    return 0;
		}
	}
	else
		return 0;
}
//---------------------------------------------------------------end:methodForReceiving

//---------------------------------------------------------------end:methodsForBluetooth
