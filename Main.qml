import QtQuick
import ContactsModel 1.0

Window {
    id: root
    width: Screen.width
    height: Screen.height
    visible: true
    title: qsTr("Contacts")
    color: "#EBEDEF"
    ContactsModel {
        id: contactsModel
    }
    ListView {
        id: listView
        anchors {top: header.bottom}
        width : parent.width
        height : parent.height - header.height
        model: contactsModel


        delegate: Rectangle {
            id: delRoot
            width: ListView.view.width
            height: 40
            radius: 15
            border.width: 0.5
            border.color: "#EBEDEF"
            Rectangle {
                anchors { verticalCenter: parent.verticalCenter; left: parent.left; leftMargin: 20 }
                width: 30
                height: 30
                radius: 15
                color: getRandomHexColor()
                Text {
                    id: intials
                    anchors.centerIn : parent
                    font { pixelSize: 18; }
                    color: "white"
                    text: name.charAt(0)
                }
            }
            Text {
                anchors {verticalCenter: parent.verticalCenter; left: parent.left; leftMargin: 80}
                font {pixelSize: 16}
                text: name
            }
            MouseArea {
                anchors.fill: parent
                onClicked: {
                    console.log("clicked")
                }
            }
        }

    }
    Rectangle {
        id: header
        width: parent.width;
        height: 70
        anchors.top: parent.top
        color: "#EBEDEF"

        Text {
            anchors {verticalCenter: parent.verticalCenter; left: parent.left; leftMargin: 30}
            text: "Contacts"
            font.pixelSize: 18
        }
        Row {
            anchors {verticalCenter: parent.verticalCenter; right: parent.right; rightMargin: 20}
            spacing: 10
            Button {
                label.text: "➕"

            }
            Button {
                label.text: "🔍"
            }
        }




    }
    function getRandomHexColor() {
        const randomInt = Math.floor(Math.random() * 0xFFFFFF);
        const hexString = randomInt.toString(16).padStart(6, '0');
        return `#${hexString}`;
    }


}
