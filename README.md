# README #

The streaming framework consists of an Android application and a Java app Server. It is a cloud based solution for real time analytics on streamed data from smartphones. The main goal of the framework is to build a distributed system that control several android phones as well as run statistical signal processing and machine learning algorithms on real time streamed data (Audio, Sensor data ...).
The final goal is to build an IoT cloud based platform for real time analytics and intelligence. 
We first focused on running algorithms on audio data. Therefore a framework that is able to stream high quality audio data and launch real time computations on the server has been developed.   
The framework offers these main features:

* **Easy way to control the connected smartphones and sending detailed instructions such as: **

     * **Record high quality audio data and stream it in real time to the server**

     * **Stream high quality audio data to the Android phones from the server**

     * **Redirect audio streams between phones**

* **Display real time Fourier transform of the streamed signals**

The architecture of the framework has been made such that everything is controlled by the server. The server sends detailed instructions to the phones to perform certain operations such as recording and streaming audio with specific parameters. 
The configuration of the framework has been made as simple as possible. The user has just to start the server application and then run the Android app in the phone, all connections will be made automatically. A complete communication protocol has been implemented so that the solutions can be extended to run on any connected device.

The framework can be used by beginners, students or researchers to either play with audio signals and observe some interesting properties in such as the real time display of the Fourier transform.   
The framework is still under development and not all signal processing and machine learning algorithms are integrated.