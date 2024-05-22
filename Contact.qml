import QtQuick

Rectangle {
    id:contactRoot
    width: Screen.width
    height: Screen.height
    color: "#EBEDEF"

    property var callBack: ()=>{}
    MouseArea {anchors.fill: parent}

    clip: true
    Column {
        id:column
        anchors { fill: parent; margins: 20; topMargin: 50}
        spacing: 20
        CustomTextInput {
            id: fullName
            placeHolder { text: "Full name"}

        }
        CustomTextInput {
            id: phoneNumber
            placeHolder { text: "Phone number"}

        }

        Row {
            anchors.horizontalCenter: parent.horizontalCenter
            spacing: 10
            Button {
                label.text: "Save"
                label.color: "white"
                background.color: "green"
                onClicked: {
                    contactRoot.destroy()
                    //to be implemented
                }
            }
            Button {
                label.text: "Cancel"
                label.color: "white"
                background.color: "red"
                onClicked: {
                    contactRoot.destroy()
                }
            }


        }

    }


    Header {
        title: "Create Contact"
    }

}
