# Ultimate-Pool-Simulator

## Overview
A realistic approach on simulating 8 ball pool built with `java.swing` as the GUI and a Tiva-C Launchpad as the controller.
The rules were coded to follow the official World Pool-Billiard Association (WPA) 8 ball rules as much as possible. Made for the final
project of SE 101.

## Physics Engine
The core component behind the simulation is the physics engine. Each ball is modelled as a 2D circle and each border is modelled as a 2D line.
Collisions were detected using circle-line and circle-circle intersection algorithms. Each ball possesses a translational and an
angular velocity to account for spin. The collisions were modelled under a near perfect environment as inelastic collisions with 
a fixed coefficient of restitution. When a ball has achieved perfectly, it will be subject to a drastically lower friction (rolling friction).

## Tiva Controls
 - The potentiometer was used to adjust the direction to hit the cue ball and to select the pocket to hit the eight ball into.
 - The accelerometer was used to detect the initial velocity of the cue ball when hitting the ball and to adjust the cue ball position when
 placing it after ball in hand.
 - The buttons were mainly used to confirm actions (I.E. selecting the pocket for the eight ball and confirming the location of the cue ball
 after ball in hand)
 - Switch 2 was used to switch from aiming mode to shooting mode.

## Java Class Structure
| Class Name              | Role                                                                                                           |
|-------------------------|----------------------------------------------------------------------------------------------------------------|
| Main.java               | Entry object for GUI                                                                                           |
| MainFrame.java          | Main wrapper object for GUI. Handles communication between UserPanel, GamePanel, and Tiva-C.                   |
| UserPanel.java          | User object that represents each player.                                                                       |
| GamePanel.java          | Game object that represents the pool table.                                                                    |
| Ball.java               | Object that represents the pool balls and pockets.                                                             |
| Line.java               | Object that represents the border of the pool table.                                                           |
| Vector.java             | Object that represents both points and direction vectors. Used in Ball, Line and the collision detection code. |
| SerialCommunicator.java | Object that reads and writes to the serial port. Communicates with Tiva-C.                                     |
