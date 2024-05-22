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
    Component {
        id: sectionHeading
        Text {
            text: section.charAt(0).toUpperCase()
            font.bold: true
            font.pixelSize: 16
            x : 30
        }
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

        section.property: "name"
        section.criteria: ViewSection.FirstCharacter
        section.delegate: sectionHeading
    }

    Header{
        id: header
        addOrSave {
            label.text : "➕"
            onClicked : Qt.createComponent("Contact.qml").createObject(root, {callBack: ()=>{}})
            type: "addContact"
            visible: true
        }
        searchOrcancel {
            label.text : "🔍"
            type: "searchContact"
            visible: true
        }

    }

    function getRandomHexColor() {
        const randomInt = Math.floor(Math.random() * 0xFFFFFF);
        const hexString = randomInt.toString(16).padStart(6, '0');
        return `#${hexString}`;
    }
}
