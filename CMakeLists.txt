cmake_minimum_required(VERSION 3.16)

project(ContactsListApp VERSION 0.1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD_REQUIRED ON)

find_package(Qt6 6.4 REQUIRED COMPONENTS Quick)

qt_standard_project_setup()

qt_add_executable(appContactsListApp
    main.cpp
)

qt_add_qml_module(appContactsListApp
    URI ContactsListApp
    VERSION 1.0
    QML_FILES Main.qml
QML_FILES Button.qml
SOURCES ContactsModel.h
SOURCES ContactsModel.cpp
QML_FILES ContactView.qml
QML_FILES Header.qml
QML_FILES CustomTextInput.qml
       )

# Qt for iOS sets MACOSX_BUNDLE_GUI_IDENTIFIER automatically since Qt 6.1.
# If you are developing for iOS or macOS you should consider setting an
# explicit, fixed bundle identifier manually though.
set_target_properties(appContactsListApp PROPERTIES
#    MACOSX_BUNDLE_GUI_IDENTIFIER com.example.appContactsListApp
    MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION}
    MACOSX_BUNDLE_SHORT_VERSION_STRING ${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}
    MACOSX_BUNDLE TRUE
    WIN32_EXECUTABLE TRUE
    QT_ANDROID_PACKAGE_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/android
)

target_link_libraries(appContactsListApp
   PRIVATE Qt6::Quick
)

include(GNUInstallDirs)
install(TARGETS appContactsListApp
    BUNDLE DESTINATION .
    LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
    RUNTIME DESTINATION ${CMAKE_INSTALL_BINDIR}
)

qt_add_resources(appContactsListApp "android"
    PREFIX /
    FILES
        android/src/com/example/contactslist/MainActivity.java
        android/AndroidManifest.xml
        android/build.gradle
        android/res/values/libs.xml
)
if(ANDROID)
    set_property(TARGET Qt::Core APPEND PROPERTY INTERFACE_COMPILE_DEFINITIONS
        QT_ANDROID_EXPORTED_TARGET=appContactsListApp)
    set_property(TARGET Qt::Core APPEND PROPERTY INTERFACE_COMPILE_FEATURES
        cxx_std_17)
endif()
