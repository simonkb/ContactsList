#include "ContactsModel.h"
#include <jni.h>
ContactsModel::ContactsModel(QObject *parent)
    : QAbstractListModel(parent) {
    fetchContacts();
}
extern "C" JNIEXPORT void JNICALL Java_com_example_contactslist_MainActivity_onContactsLoaded(JNIEnv *env, jobject obj, jstring n, jlong ptr) {
    QString contacts = env->GetStringUTFChars(n, nullptr);
    ContactsModel *contactsModel = (ContactsModel*)ptr;
    if(contactsModel)
        contactsModel->populateModelWithJson(contacts);
}
void ContactsModel::addContact(const QVariantMap &contact) {
    beginInsertRows(QModelIndex(), rowCount(), rowCount());
    m_contacts << contact;
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
    default:
        return QVariant();
    }
}

QHash<int, QByteArray> ContactsModel::roleNames() const {
    QHash<int, QByteArray> roles;
    roles[NameRole] = "name";
    roles[PhoneNumberRole] = "phoneNumber";
    roles[SelectedRole] = "selected";
    return roles;
}
void ContactsModel::populateModelWithJson(const QString &jsonString) {
    QVariantList list = QJsonDocument::fromJson(jsonString.toUtf8()).toVariant().toList();
    m_contacts.clear();
    for(QVariant contact: list ){
        addContact(contact.toMap());
    }
}
void ContactsModel::fetchContacts(){
    QJniObject javaClass = QNativeInterface::QAndroidApplication::context();
    javaClass.callMethod<void>("fetchContacts", "(J)V", (long long)this);

}
