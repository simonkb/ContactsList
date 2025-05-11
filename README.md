# ContactsList

ContactsList is a Android contact management application built using C++, QML, and CMake. It offers a responsive and intuitive user interface for managing contact information.

## Features

* **Add and View Contacts**: Easily add new contacts and view existing ones.
* **Dynamic UI**: Responsive design using QML for a seamless user experience.
* **Data Binding**: Synchronization between the C++ backend and QML frontend.

## Technologies Used

* **C++**: Backend logic and data management.
* **QML**: User interface components.
* **CMake**: Build system for cross-platform compatibility.

## Getting Started

### Prerequisites

* Qt Framework (Qt 5 or 6)
* CMake
* C++ Compiler (e.g., GCC, Clang, MSVC)

### Build Instructions

1. Clone the repository:

   ```bash
   git clone https://github.com/simonkb/ContactsList.git
   cd ContactsList
   ```

2. Create a build directory:

   ```bash
   mkdir build && cd build
   ```

3. Run CMake:

   ```bash
   cmake ..
   ```

4. Build the project:

   ```bash
   cmake --build .
   ```

5. Run the application:

   ```bash
   ./ContactsList
   ```

## Project Structure

* `main.cpp`: Application entry point, sets up the QML engine.
* `ContactsModel.cpp/h`: C++ model for managing contacts and exposing data to QML.
* `QML/`: Directory containing all user interface files including:

  * `Main.qml`: Main UI layout.
  * `ContactView.qml`: Displays contact details.
  * `CustomTextInput.qml`: Reusable text input field.
  * `Button.qml`: Custom button component.
  * `Header.qml`: Top UI header for screens.
* `CMakeLists.txt`: Build configuration.

## License

This project is licensed under the MIT License.
