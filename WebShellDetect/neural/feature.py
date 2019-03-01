import os
import pickle

from WebShellDetect.settings import BASE_DIR
from shelldata import load_opcode_dict

# 加载 idf 字典文件
def load_idf():
  with open(os.path.join(BASE_DIR, "php.opcode.idf.pickle"), "rb") as f:
    idf = pickle.load(f)
  return idf

def make_feature_count(opdict: dict, opcodes: str):
  """
  根据 opcode 的序列，返回对应的特征向量
  :param opdict:
  :return: 基于计数的 199 (因为统计出一共 199 个 opcode）维，每一维对应某个 opcode 关键词的特征向量
  """
  dim = len(opdict) # = 199
  feature = [0] * dim
  for x in opcodes.strip().split():
    feature[opdict[x]] += 1
  return feature

def make_feature_count_v2(opdict: dict, opcodes: list):
  """
  根据 opcode 的序列，返回对应的特征向量
  :param opdict:
  :return: 基于计数的 199 (因为统计出一共 199 个 opcode）维，每一维对应某个 opcode 关键词的特征向量
  """
  dim = len(opdict) # = 199
  feature = [0] * dim
  for x in opcodes:
    feature[opdict[x]] += 1
  return feature

def make_feature_tfidf(opdict: dict, opcodes: str, idf: dict):
  feature = make_feature_count(opdict, opcodes)
  opcodes_list = opcodes.strip().split()
  for x in set(opcodes_list):
    feature[opdict[x]] *= idf[x]
  return feature

def make_feature_tfidf_v2(opdict: dict, opcodes: list, idf: dict):
  feature = make_feature_count_v2(opdict, opcodes)
  for x in set(opcodes):
    feature[opdict[x]] *= idf[x]
  return feature

def make_feature_one_hot(opdict: dict, opcode):
  feature = [0] * len(opdict)
  feature[opdict[opcode]] = 1
  return feature

if __name__ == '__main__':
  opdict = load_opcode_dict()
  idf = load_idf()
  opcodes = """DEFINED JMPNZ EXIT VERIFY_INSTANCEOF RETURN RECV RECV RECV IS_IDENTICAL JMPZ INIT_STATIC_METHOD_CALL SEND_VAR_EX SEND_VAR_EX SEND_VAR_EX DO_FCALL RETURN IS_IDENTICAL JMPZ ASSIGN FETCH_OBJ_R INIT_METHOD_CALL SEND_VAR_EX DO_FCALL CONCAT QM_ASSIGN QM_ASSIGN COUNT JMP FETCH_DIM_R FETCH_DIM_R IS_NOT_IDENTICAL JMPZ FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA JMP INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FETCH_DIM_W ASSIGN_DIM OP_DATA FETCH_DIM_IS ISSET_ISEMPTY_DIM_OBJ JMPNZ FETCH_OBJ_R INIT_METHOD_CALL SEND_VAR_EX DO_FCALL CONCAT FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FAST_CONCAT FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA IS_IDENTICAL JMPZ FETCH_DIM_IS ISSET_ISEMPTY_DIM_OBJ JMPNZ FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT FAST_CONCAT FETCH_OBJ_R INIT_METHOD_CALL GOTO FETCH_DIM_FUNC_ARG FETCH_DIM_FUNC_ARG UNKNOWN DO_FCALL CONCAT ASSIGN_DIM OP_DATA FETCH_DIM_R FETCH_DIM_R CONCAT ASSIGN_DIM OP_DATA PRE_INC IS_SMALLER JMPNZ CONCAT FAST_CONCAT ASSIGN_CONCAT COUNT IS_IDENTICAL JMPZ FETCH_DIM_R QM_ASSIGN JMP INIT_FCALL SEND_VAL SEND_VAR DO_ICALL FAST_CONCAT FAST_CONCAT QM_ASSIGN ASSIGN_CONCAT INIT_FCALL SEND_REF SEND_VAR DO_ICALL RETURN RECV RECV RETURN"""
  print(make_feature_count(opdict, opcodes))
  print(make_feature_tfidf(opdict, opcodes, idf))
  print(make_feature_one_hot(opdict, "EXIT"))
