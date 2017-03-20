
#define SUCCESS	1
#define ERROR	0

#define SCARD_UNKNOWN			0x0001	/*!< Unknown state */
#define SCARD_UART_NOT_CONNECTED			0x0002	/*!< UART Not connected   */
#define SCARD_ABSENT			0x0003	/*!< Card is absent */
//smartCOS Create_File
#define SCARD_COMMAND_EXECUTED_CORRECTLY			0x0004	/*!< Command executed correctly  */
#define SCARD_WRITE_EEPROM_FAIL			0x0005	/*!< Write EEPROM failure   */
#define SCARD_DATA_LENGTH_ERROR			0x0006	/*!< Data length error    */
#define SCARD_ALLOW_CODE_TRANSFER_ERROR_COUNT			0x0007	/*!< Allow the code transfer error count    */
#define SCARD_CREATE_CONDITION_NOT_SATISFIED			0x0008	/*!< Create condition not satisfied     */

#define SCARD_SECURITY_CONDITION_NOT_SATISFIED			0x0007	/*!< Security condition is not satisfied     */
#define SCARD_IDENTIFIER_ALREADY_EXISTS			0x0008	/*!< Identifier already exists      */
#define SCARD_FUNCTION_NOT_SUPPORTED			0x0009	/*!< Function not supported       */
#define SCARD_FILE_NOT_FOUND			0x0011	/*!< File not found        */
#define SCARD_NOT_ENOUGH_SPACE			0x0012	/*!< Not enough space */
#define SCARD_PAREMETER_IS_INCORRECT			0x0013	/*!< The parameter is incorrect         */
#define SCARD_INS_IS_INCORRECT			0x0014	/*!< The INS is incorrect         */
#define SCARD_CLA_IS_INCORRECT			0x0015	/*!< The CLA is incorrect         */

//Write_KEY
#define SCARD_CMD_NOT_MATCH_TYPES			0x0016	/*!< Command file types do not match        */
#define SCARD_KEY_LOCK			0x0017	/*!< Key lock         */
#define SCARD_GET_RANDOM_INVALID			0x0018	/*!< From a random number is invalid         */
#define SCARD_CONDITION_OF_USE_NOT_SATISFIED			0x0019	/*!< Conditions of use does not satisfied          */
#define SCARD_MAC_INCORRECT			0x0020	/*!< MAC is incorrect          */
#define SCARD_DATA_NOT_CORRECT			0x0021	/*!< Data domain is not correct           */
#define SCARD_CARD_LOCK			0x0022	/*!< Card lock            */
#define SCARD_FILE_SPACE_INSUFFICIENT			0x0023	/*!< File space is insufficient              */
#define SCARD_P1_AND_P2_NOT_CORRECT			0x0024	/*!< P1 and P2 not correct            */
#define SCARD_APP_PERMANENT_LOCK			0x0025	/*!< Application  permanent lock             */
#define SCARD_KEY_NOT_FOUND			0x0026	/*!< KEY is not found             */

#define SCARD_NOT_BINARY_FILE			0x0027	/*!< not binary file           */
#define SCARD_CONDITION_OF_READ_NOT_SATISFIED			0x0028	/*!< the conditions of read does not satisfied           */
#define SCARD_CONDITION_OF_CMD_NOT_SATISFIED			0x0029	/*!< the condition of command execution does not satisfied           */
#define SCARD_RECORD_NOT_FOUND			0x0030	/*!< record not found        */
#define SCARD_NO_DATA_RETURN			0x0031	/*!< Card no data can be returned       */

#define SCARD_SECURITY_DATA_NOT_CORRECT			0x0032	/*!< Security message data item is not correct        */
#define SCARD_P1_AND_P2_OUT_OF_GAUGE			0x0033	/*!< P1 and P2 are out of gauge            */
#define SCARD_FILE_NOT_LINEAR_FIXED_FILE			0x0034	/*!< The file is not a linear fixed length file             */
#define SCARD_APP_TEMPORARY_LOCED			0x0035	/*!< APP Temporary locked           */
#define SCARD_FILE_STORAGE_SPACE_NOT_ENOUGH			0x0036	/*!< File storage space is not enough          */
#define SCARD_NOT_EXTERNAL_AUTHENTICATION_KEY		0x0037	/*!< Instead of external authentication key           */
#define SCARD_Key_CONDITION_NOT_SATISFIED 			0x0038	/*!< Key using the condition is not satisfied          */
#define SCARD_AUTHENTICATION_METHOD_LOCKED 			0x0039	/*!< Authentication method locked         */
#define SCARD_KEY_FILE_NOT_FOUND 			0x0040	/*!< Key File not found           */
#define SCARD_SAFETY_INFORMATION_NOT_CORRECT 			0x0041	/*!< Safety information is not correct         */
#define SCARD_MALLOC_FAILURE 			0x0042	/*!< Malloc Failure         */
#define SCARD_KEY_VALUE_IS_ERROR			0x1000	/*!< KEY value is error          */

typedef enum {
	MASTER_FILE, KEY_MASTER_FILE, DEDICATED_FILE, ELEMENTARY_FILE,
} File_TYPE;

typedef enum {
	IC_SMARTCOS, IC_TIMECOS
} CARD_TYPE;

typedef enum {
	INSTALL_KEY, MODIFY_KEY
} KEY_OPERATE;

typedef enum {
	PROCLAIMED, CIPHERTEXT
} KEY_INSTALL_MODE;

typedef struct {
	KEY_OPERATE KEY_Opt;
	unsigned char* Key_MsgData;
	unsigned int Key_Msglen;
} KEY_PROCLAIMED;

typedef struct {
	KEY_OPERATE KEY_Opt;
	unsigned int KEY_Type; //00:install,XX:KEY TYPE
	unsigned int KEY_ID;
	unsigned char* Key_InforData;
	unsigned int Key_Datalen;
} KEY_CIPHERTEXT;

typedef struct {
	KEY_INSTALL_MODE KEY_InMode;
	KEY_PROCLAIMED* PL;
	KEY_CIPHERTEXT* Cht;
	unsigned int KeyType;
	unsigned char KeyID;
	unsigned int KeyLen;
	unsigned char* KeyHeader;
	unsigned char* KeyValue;
} KEY_MODE;

typedef struct {
	unsigned char TransCode[8];
	unsigned char Authority[1];
	unsigned char FileId[1];
	unsigned char* FileName;
	unsigned int NameLen;
} CMF;

typedef struct {
} CKF;

typedef struct {
	unsigned char FileId[2];
	unsigned char FileLen[2];
	unsigned char Authority[1];
	unsigned char Authority1[1];
	unsigned char* FileName;
	unsigned int NameLen;
} CDF;

typedef struct {
	unsigned char FileId[2];
	unsigned char FileType[1];
	unsigned char FileLen[2];
	unsigned char Authority1[1];
	unsigned char Authority2[1];
	unsigned char Len1[1];
	unsigned char Len2[1];
} CEF;

typedef struct {
	CARD_TYPE SMART_ID;
	unsigned int File_Type;
	//unsigned int Filelen;
	CMF* mf;
	CKF* kf;
	CDF* df;
	CEF* ef;
} CreateFile_MSG;

typedef struct {
	CARD_TYPE SMART_ID;
	KEY_MODE *pKEY;
} KEY_MSG;

typedef struct {
	unsigned int FileId;
	unsigned int Offset;
	unsigned int level;
	unsigned char* UBData;
	unsigned int UBDatalen;
	unsigned int Count;
	unsigned char* pszRecBuf;
} BINARY_MSG;

typedef struct {
	unsigned char IBANCode[8];
} IBAN_MSG;

typedef struct {
	unsigned int FileId;
	unsigned int Index;
	unsigned int level;
	unsigned char* UBData;
	unsigned int UBDatalen;
	unsigned int Count;
	unsigned char* pszRecBuf;
} Record_MSG;

typedef struct {
	unsigned int FileType;
	unsigned int FileIndex;
	unsigned char* SFData;
	unsigned int SFDatalen;
	unsigned char* pszRecBuf;
} SelectFile_MSG;

typedef struct {
	unsigned char KeyID;
	unsigned char encrypt[8];
} ExternAuth_MSG;

typedef struct {
	unsigned char* pszRecBuf;
	unsigned int Count;
} Challenge_MSG;

typedef struct {
	unsigned char* pszRecBuf;
	unsigned int Count;
} Response_MSG;

unsigned int Uart_Open(void);

void Uart_Close(void);

unsigned int Uart_Send(unsigned char* pData, unsigned int dwLen);

unsigned int Uart_Recv(unsigned char* pData, unsigned int* dwLen);

unsigned int IC_Send_Recv(unsigned char* pRequestData, unsigned int dwReqlen,
		unsigned char* pResponseData, unsigned int* dwRsplen);

unsigned char IC_System_Reset(unsigned char* pRecBuf);

unsigned char IC_ResetCard(unsigned char* pRecBuf);

//建立文件
unsigned char IC_CreateFile(CreateFile_MSG* msg);
//结束建立文件
unsigned char IC_CreateFile_End(CreateFile_MSG* msg);
//增加或修改密钥
unsigned char IC_Write_KEY(KEY_MSG* msg);
//读银行账号
unsigned char IC_Read_IBAN(IBAN_MSG* msg);
//读二进制
unsigned char IC_Read_Binary(BINARY_MSG* msg);
//修改二进制
unsigned char IC_Update_Binary(BINARY_MSG* msg);
//读记录
unsigned char IC_Read_Record(Record_MSG* msg);
//追加记录
unsigned char IC_Append_Record(Record_MSG* msg);
//修改记录
unsigned char IC_Update_Record(Record_MSG* msg);
//选择文件
unsigned char IC_Select_File(SelectFile_MSG* msg);
//圈存
unsigned char IC_Credit_For_Load(void);
//消费/取现
unsigned char IC_Debit_For_Purchase(void);
//圈提
unsigned char IC_Debit_For_Unload(void);
//读余额
unsigned char IC_Get_Balance(void);
//取交易认证
unsigned char IC_Get_Transaction_Prove(void);
//取现初始化
unsigned char IC_Initialize_For_Case_Withdraw(void);
//圈存初始化
unsigned char IC_Initial_For_Load(void);
//消费初始化
unsigned char IC_Initial_For_Purchase(void);
//圈提初始化
unsigned char IC_Initial_For_Unload(void);
//修改初始化
unsigned char IC_Initial_For_Update(void);
//修改透支限额
unsigned char IC_Update_Overdraw_Limit(void);
//应用锁定
unsigned char IC_Application_Block(void);
//应用解锁
unsigned char IC_Application_Unlock(void);
//卡片锁定
unsigned char IC_Card_Block(void);
//外部认证
unsigned char IC_External_authentication(ExternAuth_MSG* msg);
//产生随机数
unsigned char IC_Get_Challenge(Challenge_MSG* msg);
//取响应
unsigned char IC_Get_Response(Response_MSG* msg);
//内部认证
unsigned char IC_Internal_Authentication(void);
//修改/解锁PIN
unsigned char IC_PIN_Change_OR_Unblock(void);
//校验PIN
unsigned char IC_Verify_PIN(void);
//修改PIN
unsigned char IC_Change_PIN(void);
//重装PIN
unsigned char IC_Reload_PIN(void);
//安全模块指令
unsigned char IC_Crypt(void);
//生成过程密钥
unsigned char IC_Generate_KEY(void);
