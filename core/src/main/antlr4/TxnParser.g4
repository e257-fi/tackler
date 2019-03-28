/*
 * Copyright 2016-2019 E257.FI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
parser grammar TxnParser;

options {
    tokenVocab = TxnLexer;
    language = Java;
}

txns: blankline* txn (blankline+ txn)* blankline* opt_sp EOF;

txn: date code? (description | opt_sp) NL txn_meta? txn_comment* postings;

date: DATE
    | TS
    | TS_TZ
    ;

code: sp '(' code_value  ')';


code_value: ~( '\'' | '(' | ')' | '[' | ']' | '{' | '}' | '<' | '>' | NL)*;

description: sp '\'' text;

text: ~(NL)*;

txn_meta: indent '#' sp txn_meta_uuid NL;


txn_meta_uuid: UUID_NAME ':' sp UUID_VALUE opt_sp;

txn_comment: indent comment NL;

indent: (' '|'\t')+;

comment: ';' ' ' text;

postings: posting+ (posting|last_posting);

posting:  indent account sp amount opt_unit? (opt_comment | opt_sp) NL;

last_posting: indent account (opt_comment | opt_sp) NL;


opt_unit: sp unit opt_position?;

opt_comment: opt_sp comment;


opt_position: opt_opening_pos
    | opt_opening_pos  closing_pos
    | closing_pos
    ;

opt_opening_pos: sp '{' opt_sp amount sp unit opt_sp '}';

closing_pos: sp '@' sp amount sp unit;

account: ID (':' (ID|SUBID|INT))*;

amount: INT|NUMBER;

unit: ID;

sp: (' '|'\t')+;
opt_sp: (' '|'\t')*;

blankline: opt_sp NL;
