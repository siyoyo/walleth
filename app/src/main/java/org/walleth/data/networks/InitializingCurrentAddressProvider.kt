package org.walleth.data.networks

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.kethereum.model.Address
import org.walleth.data.AppDatabase
import org.walleth.data.DEFAULT_PASSWORD
import org.walleth.data.addressbook.AddressBookEntry
import org.walleth.data.config.Settings
import org.walleth.data.keystore.WallethKeyStore

class InitializingCurrentAddressProvider(keyStore: WallethKeyStore, appDatabase: AppDatabase, settings: Settings) : CurrentAddressProvider(settings) {

    init {
        val lastAddress = settings.accountAddress
        if (lastAddress != null) {
            value = Address(lastAddress)
        } else {
            async(CommonPool) {
                if (keyStore.getAddressCount() > 0) {
                    postValue(keyStore.getAddressByIndex(0))
                } else {
                    val newAccountAddress = keyStore.newAddress(DEFAULT_PASSWORD)
                    postValue(newAccountAddress)
                    appDatabase.addressBook.upsert(AddressBookEntry(
                            name = "Default Account",
                            address = newAccountAddress,
                            note = "This Account was created for you when WALLΞTH started for the first time",
                            isNotificationWanted = true,
                            trezorDerivationPath = null
                    ))
                }
            }
        }
    }

}