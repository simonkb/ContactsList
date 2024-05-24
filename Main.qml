import QtQuick
import ContactsModel 1.0

Window {
    id: root
    width: Screen.width
    height: Screen.height
    visible: true
    title: qsTr("Contacts")
    color: "#EBEDEF"
    property int selectedCount: 0
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
            color: selected ? Qt.lighter("orange") : "white"
            Rectangle {
                anchors { verticalCenter: parent.verticalCenter; left: parent.left; leftMargin: 20 }
                width: 30
                height: 30
                radius: 15
                color: selected ? "orange" : getRandomHexColor()
                Text {
                    id: intials
                    anchors.centerIn : parent
                    font { pixelSize: 18; }
                    color: "white"
                    text: selected ? "✓" : name.charAt(0)
                }
            }
            Text {
                anchors {verticalCenter: parent.verticalCenter; left: parent.left; leftMargin: 80}
                font {pixelSize: 16}
                text: name
            }

            MouseArea {
                anchors.fill: parent
                onPressAndHold: {
                    if (!selected){
                        selectedCount +=1
                        selected = true
                    }
                }
                onClicked: {
                    if (selectedCount > 0){
                        selectedCount += selected ? -1 : 1
                        selected = !selected
                    } else {
                        Qt.createComponent("Contact.qml").createObject(root, {callBack: contactsModel.onSaveContactsClicked, contactModel: model})
                    }
                }
            }
        }

        section.property: "name"
        section.criteria: ViewSection.FirstCharacter
        section.delegate: Text {
            text: section.charAt(0).toUpperCase()
            font.bold: true
            font.pixelSize: 16
            x : 30
        }
    }

    Header{
        id: header
        title: selectedCount > 0 ? selectedCount + " Selected" : "Contacts"
        addOrSave {
            label { text : selectedCount > 0 ? "Delete" : "➕" ; color: selectedCount > 0 ? "white" : "black" }
            onClicked : {
                if(selectedCount > 0) {
                    contactsModel.onDeleteContactsClicked()
                    selectedCount = 0
                } else {
                    Qt.createComponent("Contact.qml").createObject(root, {callBack: contactsModel.onSaveContactsClicked})
                }
            }
            background {
                visible: selectedCount > 0; color: "red"
            }
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
