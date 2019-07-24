# FTP
The graduation project: implementation of file transfer system supporting breakpoint retransmission

## UI
The project consists of two parts: client and server.

### client ui

The following figure is the main pane of client
![main pane of client](screenshot/client_main_pane.png "main pane of client")

This project provide user registration function
![register_dialog of client](screenshot/client_register_dialog.png "register_dialog of client")

Next figure is used for users to modify configuration parameters
![settings dialog of client](screenshot/client_setting_dialog.png "settings dialog of client")

There is "about dialog" in both client and server
![about dialog](screenshot/about_dialog.png "about dialog")

### server ui

The server main panel mainly contains a text area to record communication message
![main pane of server](screenshot/server_main_pane.png "main pane of server")

The server also provides a configuration parameter modification dialog.
![settings dialog of server](screenshot/server_setting_dialog.png "settings dialog of server")

The server provides comprehensive user management function
![user management dialog of server](screenshot/server_user_management_dialog.png "user management dialog of server")

## Project functional structure

Detailed project functions are listed in the functional structure diagram
![functional structure diagram of client](screenshot/client_functional_structure.png "functional structure diagram of client")
![functional structure diagram of server](screenshot/server_functional_structure.png "functional structure diagram of server")

The network security communication model used by the project is shown in the figure below, which includes the encryption algorithms Base64, MD5, AES and RSA.
![encryption_model](screenshot/encryption_model.png "encryption model")
