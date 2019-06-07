# CoinUs Wallet Core for Android 

Basic architecture used in CoinUs Wallet for Android.
Interaction with CoinVerse BNUS converter is contained in WalletHDEtherPocket.


#### Key Files

| File Name                | Description                                                                 |
| ------------------------ | ----------------------------------------------------------------------------|
| CoinUsWallet             | Base wallet class for CoinUs Wallet. Manages all wallet related operations. |
| CoinUsServiceWallet      | Common interface for CoinUs wallet basic functions.                         |
| EtherWalletBase          | Ethereum wallet base abstract class.                                        |
| WalletHDEtherPocket      | Wrapper class that holds Ethereum related operation. Uses Web3j to make transactions including CoinVerse Transactions. |
| CoinUsBancorAbi          | Wrapper class for CoinVerse Bancor ABI                                      |
| CoinType                 | CoinType superclass that all coin types must extend. Holds common operation schema for all crypto coins. |
| EthereumCoins            | CoinType defined for Ethereum coin                                          |
| Mnemonic                 | Holds logics for handling mnemonic words                                    |
