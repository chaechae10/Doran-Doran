import { useState } from "react";
import Button from "../commons/button";
import Email from "../commons/email";
import Input from "../commons/input";

import style from "./index.module.css";

const FindPw = () => {
  const [email, setemail] = useState<string>("");
  const [id, setId] = useState<string>("");
  return (
    <>
      <div className={style.form}>
        <Email value={email} setValue={setemail} />
        <Input value={id} setValue={setId} placeholder="회원 아이디" />
      </div>
      <div className={style.btnWrap}>
        <Button styled="danger" name="취소" onClick={() => console.log("ss")} />
        <Button name="검색" onClick={() => console.log(email, id)} />
      </div>
    </>
  );
};

const FindId = () => {
  const [email, setemail] = useState<string>("");
  return (
    <>
      <div className={style.form}>
        <Email value={email} setValue={setemail} />
      </div>
      <div className={style.btnWrap}>
        <Button styled="danger" name="취소" onClick={() => console.log("ss")} />
        <Button name="검색" onClick={() => console.log(email)} />
      </div>
    </>
  );
};

export default function HelpPage() {
  const [nowFind, setNowFind] = useState("id");

  const menu = [
    {
      value: "id",
      name: "아이디",
    },
    {
      value: "pw",
      name: "비밀번호",
    },
  ];

  return (
    <div className={style.wrap}>
      <div className={style.menu}>
        {menu.map((list) => (
          <button
            className={`${nowFind === list.value && style.active}`}
            onClick={() => setNowFind(list.value)}
          >
            {list.name}
          </button>
        ))}
      </div>
      <div className={style.content}>
        {nowFind === "id" ? <FindId /> : <FindPw />}
      </div>
    </div>
  );
}
