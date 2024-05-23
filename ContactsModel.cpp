#include "ContactsModel.h"
#include <jni.h>
#include <string>

ContactsModel::ContactsModel(QObject *parent)
    : QAbstractListModel(parent) {
    fetchContacts();
}
extern "C" JNIEXPORT void JNICALL Java_com_example_contactslist_MainActivity_onContactsLoaded(JNIEnv *env, jobject obj, jstring str, jlong ptr) {
    QString contacts = env->GetStringUTFChars(str, nullptr);
    ContactsModel *contactsModel = (ContactsModel*)ptr;
    if (contactsModel)
        contactsModel->loadDeviceContacts(contacts);
}
extern "C" JNIEXPORT void JNICALL Java_com_example_contactslist_MainActivity_onContactsChanged(JNIEnv *env, jobject obj, jstring str, jlong ptr, jstring action) {
    QString contacts = env->GetStringUTFChars(str, nullptr);
    QString actionStr = env->GetStringUTFChars(str, nullptr);
    ContactsModel *contactsModel = (ContactsModel*)ptr;
    if (contactsModel){
        if(actionStr == "delete")
            contactsModel->deleteContacts(contacts);
        else
            contactsModel->updateContacts(contacts);
    }
}

bool compareMaps(const QVariantMap &map1, const QVariantMap &map2) {
    QString name1 = map1["name"].toString();
    QString name2 = map2["name"].toString();
    return name1 < name2;
}

void sortVariantMapList(QList<QVariantMap> &list) {
    std::sort(list.begin(), list.end(), compareMaps);
}

void ContactsModel::addContact(const QVariantMap &contact) {
    beginInsertRows(QModelIndex(), rowCount(), rowCount());
    m_contacts.append(contact);
    endInsertRows();
}
void ContactsModel::deleteContact(const QVariantMap &contact){
    for (int i= 0; i<m_contacts.size(); ++i) {
        if (m_contacts.at(i).value("contactId").toInt() == contact.value("contactId").toInt() ) {
            beginRemoveRows(QModelIndex(), i, i);
            m_contacts.removeAt(i);
            endRemoveRows();
        }
    }
}
void ContactsModel :: deleteContacts(const QString &jsonString){
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    for(QVariant contact: list ){
        deleteContact(contact.toMap());
    }
}
void ContactsModel::updateContact(const QVariantMap &contact){
    for (int i= 0; i<m_contacts.size(); ++i) {
        if (m_contacts.at(i).value("contactId").toInt() == contact.value("contactId").toInt() ) {
                m_contacts[i] = contact;
                emit dataChanged(index(i), index(i));
                return;

        }
    }
    beginInsertColumns(QModelIndex(), rowCount()+1, rowCount()+1);
    m_contacts.append(contact);
    endInsertRows();
}


int ContactsModel::rowCount(const QModelIndex &parent) const {
    Q_UNUSED(parent);
    return m_contacts.size();
}

QVariant ContactsModel::data(const QModelIndex &index, int role) const {
    if (index.row() < 0 || index.row() >= m_contacts.size())
        return QVariant();

    const QVariantMap &contact = m_contacts.at(index.row());
    switch (role) {
    case NameRole:
        return contact["name"];
    case PhoneNumberRole:
        return contact["phoneNumber"];
    case SelectedRole:
        return contact["selected"];
    case ContactId :
        return contact["contactId"];
    default:
        return QVariant();
    }
}

QHash<int, QByteArray> ContactsModel::roleNames() const {
    QHash<int, QByteArray> roles;
    roles[NameRole] = "name";
    roles[PhoneNumberRole] = "phoneNumber";
    roles[SelectedRole] = "selected";
    roles[ContactId] = "contactId";
    return roles;
}
void ContactsModel :: updateContacts(const QString &jsonString){
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    for(QVariant contact: list ){
        updateContact(contact.toMap());
    }
    sortVariantMapList(m_contacts);
}

void ContactsModel::loadDeviceContacts(const QString &jsonString) {
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    m_contacts.clear();
    for(QVariant contact: list ){
        addContact(contact.toMap());
    }
    sortVariantMapList(m_contacts);
}
void ContactsModel::fetchContacts(){
    QJniObject javaClass = QNativeInterface::QAndroidApplication::context();
    javaClass.callMethod<void>("fetchContacts", "(J)V", (long long)this);
}
void ContactsModel::setSelected(string str, bool isSelected){
    for (int i =0; i < m_contacts.size(); ++i ){
        if(m_contacts.at(i).value("contactID").toInt() == std::stoi(str)){
            m_contacts.at(i).value("selected").setValue(isSelected);
            emit dataChanged(index(i), index(i));
        }
    }
}

void ContactsModel::onDeleteContactsClicked () {
    QStringList ids;
    for (const QVariantMap &map : m_contacts) {
        ids.append(map.value("contactId").toString());
    }
    QString idsString = ids.join(",");
    QJniObject javaClass = QNativeInterface::QAndroidApplication::context();
    javaClass.callMethod<void>("deleteSelectedContacts", "(J)V", (long long)this);
}

