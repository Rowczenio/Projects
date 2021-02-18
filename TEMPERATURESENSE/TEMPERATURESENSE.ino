
/* The aim here is to take in a input from two temperature sensors and to have the NeoSensory Buzz to vibrate along the wrist band and at specific intensity
 *  which should reflect the source direction relative to the wearer and the magnitude of the temperature sensed.
 *  
 *  Practical use case could be someone who is wearing a protective suit but needs to now the souce and strength of the temperature without reglarely checking by looking.
 */
#include <neosensory_bluefruit.h>


NeosensoryBluefruit NeoBluefruit;
// NeosensoryBluefruit NeoBluefruit("F2 AD 50 EA 96 31");

int motor;
float intensity;

float x;
float y;

// The NeoSensory Buzz and Adafruit board connect over bluetooth
void setup() {
  Serial.begin(9600);
  NeoBluefruit.begin();
  NeoBluefruit.setConnectedCallback(onConnected);
  NeoBluefruit.setDisconnectedCallback(onDisconnected);
  NeoBluefruit.setReadNotifyCallback(onReadNotify);
  NeoBluefruit.startScan();

  while (!NeoBluefruit.isConnected() || !NeoBluefruit.isAuthorized()) {}
  NeoBluefruit.deviceInfo();
  NeoBluefruit.deviceBattery();
}


void loop() {
  // Reading the analogue value of the temperatures from pin A0 and A1 to digital
  int val1 = analogRead(A0);
  int val2 = analogRead(A1);

 
  // Calculate  temperatures on 0 - 1 scale
  float t1 = calculatet(val1);
  float t2 = calculatet(val2);

  // The two t values then need to be converted to (x,y) values
  x = t_xConversion(t1, t2);
  y = t_yConversion(t1, t2);
  
  if (NeoBluefruit.isConnected() && NeoBluefruit.isAuthorized()) {
    /* The intention here is two provide a 2D sense - x and y 
     *  1 dimensional location along a line and intensity is how 
     *  the information is supposed to be represented
     *  
     *  However x and y cannot just be used directly as motors are refered to by index
     *  so there is no location '1.6', for example. However here is attempted a method
     *  to interpolate between two motors to create an 'illusion' of any location vibrating
     *  rather than 4 distinct locations.
     */
    // Two motors are used in conjunction together
    // The first index is the digit of the x value i.e. 3.5 then 3 is the index
    int motor1 =  motorIndex(x);
    // Then the second motor is just the next one over.
    // As there are only 4 actuators, if motor1 is at index 3, then motor2 is at 0
    int motor2 = (motor1 > 2 ? 0 : motor1 + 1);

    // The intensity of the two motors
    float intensity1 = intensityOfFirstMotor(x, y);
    float intensity2 = intensityOfSecondMotor(x, y);

    NeoBluefruit.vibrateMotor(motor1, intensity1);
    NeoBluefruit.vibrateMotor(motor2, intensity2);
    delay(0.1);
  }
}

/* The temperatures that is able to be detected by this sensor goes from -55C to
  150C. For the purposes of everyday use, a range of -10C to 50C is chosen. The
  formula to calculate temperature according to the converted digital value is 
  TempC = val * 5 / (1023 *0.01)
  This is then mapped to a range of 0 to 1
  
  -10 < Temp < 50
  -20.46 < val < 102.3
  0 < t < 1  where t = (val + 20.46) / 122.76 */
  
float calculatet(int val){
  float t = float((val + 20.46)/122.76);

  // values with lower temperatures will just be at the min
  if (t < 0){
    return 0;
  }
  
  // values with higher temperatures will just be at the max
  else if (t > 1){
    return 1;
  }

  else { return t;}
}

/* If the source of heat is closer to t1, 
 * then the vibration should be closer to actuator at index 0.
 * Whereas if signal collcted from t2 is stronger than t1,
 * then the vibrations should occurs closer to the actuator at index 3.
 * If both t1 and t2 get sense equally, then actuators 1 and 2 would vibrate
 * at a location 1.5.
 * 
 * This should allow some understanding of from where the heat is coming from.
 */
float t_xConversion(float t1, float t2){
  float r = 1.5*(t2 - t1) + 1.5;
  return r;
}


// Used to find the index of the first actuator
int motorIndex(float x){
  // first turn float to int
  int a = (int)x;

  //then find the remainder so it is between 0 and 4
  int motorindex = a % 4;

  return motorindex;
}

/* the temperatures are averaged to reflect general intesity. This is flawed
 * as if one of the two thermistors picked up a really temperature, it would mellowed
 * out by the average of both. However, for the most part if its hot, then both thermistors
 * will likely pick that up.
 */

float t_yConversion(float t1, float t2){
  float r = (t1 + t2)/2;
  return r;
}

// Below are the methods used to distribute vibration between two neighbouring motors
/* So if there is input (x, y) that is (1.2, 0.5)  
 *  actuator at index 1 would have a higher intensity than actuator at index 2 as 1.2
 *  is closer to 1 than 2. and then overall both motors are multiplied by 0.5
 *  input (3, 1) results in actuator at index 3 to have intensity to the max.
 */
float intensityOfFirstMotor(float x, float y){
  // Find the remainder after the decimal point
  float b = abs(remainder(x, 1));

  // The closer the decimal is to 0, the higher the intensity at that motor
  float intensity = (1 - b) * y;

  // Max intensity is 1
  if (intensity > 1){
    intensity = 1;
  }

  return intensity;
}

float intensityOfSecondMotor(float x, float y){

  float b = abs(remainder(x, 1));

  // The closer the decimal is to 1, the higher the intensity at the next motor over
  float intensity = b * y;

  if (intensity > 1) {
    intensity = 1;
  }

  return intensity;
}

/* Callbacks */

void onConnected(bool success) {
  if (!success) {
    Serial.println("Attempted connection but failed.");
    return;
  }
  Serial.println("Connected!");

  // Once we are successfully connected to the wristband,
  // send developer autherization command and commands
  // to stop sound-to-touch algorithm.
  NeoBluefruit.authorizeDeveloper();
  NeoBluefruit.acceptTermsAndConditions();
  NeoBluefruit.stopAlgorithm();
}

void onDisconnected(uint16_t conn_handle, uint8_t reason) {
  Serial.println("\nDisconnected");
}

void onReadNotify(BLEClientCharacteristic* chr, uint8_t* data, uint16_t len) {
  for (int i = 0; i < len; i++) {
    Serial.write(2*data[i]);
  }
}
