# COMP 258 Final Project: Chat Client with FTP

## Project Description
[cite_start]This is our final project for COMP 258[cite: 1]. [cite_start]We extended our Chat Client to include a GUI FTP (File Transfer Protocol) Server[cite: 11]. The application allows users to chat, browse local files, upload them to the server, and download files uploaded by other users.

## Features We Added

### 1. GUI & File Browsing
* [cite_start]We added a **Browse** button to the Chat Client using `JFileChooser` so users can pick a file easily[cite: 13].
* [cite_start]We customized the GUI panels with new colors, fonts, and layouts to make it look better than the default settings[cite: 32, 33].

### 2. Uploading Files
* [cite_start]When you click the **Save** button, the client converts the selected file into an array of bytes[cite: 15, 16].
* [cite_start]It sends the file to the server using the `#ftpUpload` command[cite: 20].
* [cite_start]The server saves these files into a folder called `uploads`[cite: 23].

### 3. Downloading Files
* [cite_start]We added a Combo Box (dropdown) that shows a list of all files on the server using `#ftplist`[cite: 28].
* Users can select a file and click **Download**. [cite_start]This uses the `#ftpget` command to save the file to the client's `downloads` folder[cite: 26, 29].

## How the Commands Work
[cite_start]We used a specific envelope structure to handle the file transfer commands[cite: 18]:

| Command | Argument | Data | What it does |
| :--- | :--- | :--- | :--- |
| `#ftpUpload` | `filename` | `byte[]` | [cite_start]Uploads the file to the server's `uploads` folder[cite: 20, 23]. |
| `#ftplist` | N/A | N/A | [cite_start]Asks the server for a list of all uploaded files[cite: 25]. |
| `#ftpget` | `filename` | N/A | [cite_start]Downloads the specific file to your `downloads` folder[cite: 26]. |

## Setup Instructions
To run this project, please make sure you follow these steps so the file paths work correctly:
1.  **Clone the repo** to your computer.
2.  **Create Folders:**
    * [cite_start]Make sure there is a folder named `uploads` inside the Server directory[cite: 23].
    * [cite_start]Make sure there is a folder named `downloads` inside the Client directory[cite: 26].
3.  **Run the Server** first.
4.  **Run the Client** and connect.

## Resources Used
* [cite_start]JFileChooser Tutorial: https://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html [cite: 14]
* [cite_start]Converting Files to Bytes: https://mkyong.com/java/how-to-convert-file-into-an-array-of-bytes/ [cite: 17]

## Authors
* Hitang M.
* Parthraj V.
