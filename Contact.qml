import QtQuick

Rectangle {
    id:contactRoot
    width: parent.width
    height: parent.height
    color: "#EBEDEF"
    property var contactModel: null
    property var callBack: ()=>{}
    MouseArea { anchors.fill: parent }
    clip: true

    Column {
        id:column
        anchors { fill: parent; margins: 20; topMargin: 150}
        spacing: 20
        CustomTextInput {
            id: fullName
            placeHolder : "Full name"

        }
        CustomTextInput {
            id: phoneNumber
            placeHolder  : "Phone number"

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
