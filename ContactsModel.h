#ifndef CONTACTSMODEL_H
#define CONTACTSMODEL_H
#include <QAbstractListModel>
#include <QList>
#include <QGuiApplication>
using namespace std;
#include <QJniObject>
#include <QObject>
#include <QJsonDocument>
#include <QJsonArray>
#include <QJsonObject>
#include <QString>
class ContactsModel : public QAbstractListModel {
    Q_OBJECT

public:
    enum ContactRoles {
        NameRole = Qt::UserRole + 1,
        PhoneNumberRole,
        SelectedRole
    };

    ContactsModel(QObject *parent = nullptr);

    void addContact(const QVariantMap &contact);
    void populateModelWithJson(const QString &contacts);
    void fetchContacts();
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;

protected:
    QHash<int, QByteArray> roleNames() const override;

private:
    QList<QVariantMap> m_contacts;
};

#endif
