import api from '@/api';

// JWT 인증이 필요한 파일 다운로드
// api(axios)의 인터셉터가 Authorization 헤더에 JWT를 자동으로 추가한다.
// 서버에서 전달받은 파일은 Blob 형태로 받아 브라우저에서 다운로드한다.
export const downloadFile = async (fileUrl, filename) => {
  try {
    // 서버에 파일 요청
    // responseType: 'blob'은 파일을 바이너리(Blob) 형태로 받기 위한 설정
  // 지정하지 않으면 axios는 JSON이나 문자열로 처리하려고 하므로
  // 다운로드 파일이 손상되거나 정상적으로 저장되지 않을 수 있다.
    const response = await api.get(fileUrl, {
      responseType: 'blob',
    });

    // Blob 데이터를 브라우저에서 사용할 수 있는 임시 URL로 변환
    const blobUrl = window.URL.createObjectURL(response.data);

    // 다운로드를 위한 a 태그 생성
    const link = document.createElement('a');

    // a 태그가 다운로드할 파일의 URL 지정
    link.href = blobUrl;

    // 저장될 파일명 지정
    // filename이 없으면 기본 이름은 'download'
    link.download = filename || 'download';

    // a 태그를 DOM에 추가
    document.body.appendChild(link);

    // 클릭 이벤트를 강제로 발생시켜 다운로드 시작
    link.click();

    // 다운로드 후 a 태그 제거
    document.body.removeChild(link);

    // 생성한 임시 URL 제거(메모리 누수 방지)
    window.URL.revokeObjectURL(blobUrl);

  } catch (error) {
    // 다운로드 실패 시 오류 출력
    console.error(error);
    alert('파일 다운로드에 실패했습니다.');
  }
};
// export const downloadFile = async (fileUrl) => {
//   try {
//     const link = document.createElement('a'); // a 노드 생성
//     link.href = fileUrl; // a노드의 href에 다운로드 파일 url 설정

//     document.body.appendChild(link); // dom에 추가
//     link.click(); // click 이벤트 강제 발생
//     document.body.removeChild(link); // dom에서 제거
//   } catch (error) {
//     console.error(error);
//   }
// };
