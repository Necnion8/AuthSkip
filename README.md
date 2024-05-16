# AuthSkip
特定のプレイヤーのみオフラインモード接続を許可するBungeeCordプラグイン

## 主な機能
- プレイヤーのアカウント認証を無視し、ログイン可能にする
- オフラインUUID または カスタムなUUID に上書き
- カスタムスキンの適用 (SkinsRestoerer .skin ファイル互換)
- MCID および、接続IPアドレスによるアカウントの紐づけ
- BungeeCord はオンラインモードのまま動作します


## 前提
- BungeeCord 1.20 以上 (またはその派生)


## コマンドと権限
- オフライン接続を可能にするアカウントの登録 - `/authskip add`
> ※ アドレスを省略した場合は、1分間指定MCIDの接続が可能で、ログイン後にアドレスも紐付けされます。<br>
> 引数: `/authskip add (MCID) [Address]`<br>
> 権限: `authskip.command.authskip`
<br>

- 登録されたアカウントの削除 - `/authskip remove`
> 引数: `/authskip remove (MCID)`<br>
> 権限: `authskip.command.authskip`
<br>

- 登録されたアカウントの一覧表示 - `/authskip list`
> 権限: `authskip.command.authskip`
<br>

- コマンド実行者をオフラインモードに登録 - `/authskip setMe`
> ※ オンラインUUID とスキンが自動設定されます (可能な場合)<br>
> ※ オフラインモードの解除はできません<br>
> 権限: `authskip.command.authskip`
<br>

- スキンファイルを Mojang API からダウンロード - `/authskip generateSkin`
> ※ ダウンロードしたいスキンのプレイヤーUUID を指定する必要があります<br>
> 引数: `/authskip generateSkin (UUID)`<br>
> 権限: `authskip.command.authskip`
<br>

- 設定ファイルの再読み込み - `/authskip reload`
> 権限: `authskip.command.authskip`
